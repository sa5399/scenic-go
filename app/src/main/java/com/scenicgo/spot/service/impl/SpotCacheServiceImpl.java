package com.scenicgo.spot.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.scenicgo.common.exception.BizException;
import com.scenicgo.spot.entity.Spot;
import com.scenicgo.spot.mapper.SpotMapper;
import com.scenicgo.spot.service.SpotCacheService;
import com.scenicgo.utils.JsonUtils;
import com.scenicgo.utils.RedisConstants;
import com.scenicgo.utils.RedisData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 景点详情缓存：逻辑过期 + 异步重建 + 空值缓存。
 *
 * <p>key 不设物理 TTL，数据里带 {@code expireTime}。逻辑过期时不阻塞请求（返回旧值），
 * 后台线程抢锁重建；只有缓存整个没有（冷启动）才让请求线程排队查库，避免并发打穿。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpotCacheServiceImpl implements SpotCacheService {

    private static final TypeReference<RedisData<Spot>> CACHE_TYPE = new TypeReference<RedisData<Spot>>() {
    };

    /** 重建锁最长持有时长，防止线程挂掉后锁不释放 */
    private static final long LOCK_LEASE_SECONDS = 10L;

    private final StringRedisTemplate stringRedisTemplate;

    private final SpotMapper spotMapper;

    private final RedissonClient redissonClient;

    private final ThreadPoolTaskExecutor cacheRebuildExecutor;

    @Override
    public Spot getSpot(Long id) {
        String key = RedisConstants.CACHE_SPOT_KEY + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return loadOnColdStart(id, key);
        }

        RedisData<Spot> cached = JsonUtils.fromJson(json, CACHE_TYPE);
        if (isExpired(cached)) {
            asyncRebuild(id, key);
        }
        return cached.getData();
    }

    /** 缓存里什么都没有：抢锁串行查库，抢不到就等一小会儿再读，仍没有就直接查库兜底 */
    private Spot loadOnColdStart(Long id, String key) {
        RLock lock = redissonClient.getLock(RedisConstants.LOCK_SPOT_KEY + id);
        boolean locked = false;
        try {
            locked = lock.tryLock(0L, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                TimeUnit.MILLISECONDS.sleep(50);
                String retry = stringRedisTemplate.opsForValue().get(key);
                if (retry != null) {
                    return JsonUtils.fromJson(retry, CACHE_TYPE).getData();
                }
                return spotMapper.selectById(id);
            }
            // 双检：等锁期间别人可能已经写好了
            String current = stringRedisTemplate.opsForValue().get(key);
            if (current != null) {
                return JsonUtils.fromJson(current, CACHE_TYPE).getData();
            }
            Spot spot = spotMapper.selectById(id);
            writeCache(key, spot);
            return spot;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("加载景点缓存被中断");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void asyncRebuild(Long id, String key) {
        cacheRebuildExecutor.execute(() -> {
            RLock lock = redissonClient.getLock(RedisConstants.LOCK_SPOT_KEY + id);
            boolean locked = false;
            try {
                locked = lock.tryLock(0L, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
                if (!locked) {
                    return;
                }
                // 可能刚被别人重建好，再确认一次
                String current = stringRedisTemplate.opsForValue().get(key);
                if (current != null && !isExpired(JsonUtils.fromJson(current, CACHE_TYPE))) {
                    return;
                }
                writeCache(key, spotMapper.selectById(id));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.warn("景点缓存重建失败 id={}", id, e);
            } finally {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        });
    }

    /** 查不到也写缓存（防穿透），只是逻辑过期时间短得多；不设物理 TTL */
    private void writeCache(String key, Spot spot) {
        long ttlMinutes = spot == null
                ? RedisConstants.CACHE_NULL_TTL_MINUTES
                : RedisConstants.CACHE_SPOT_TTL_MINUTES;
        RedisData<Spot> redisData = new RedisData<>(spot, LocalDateTime.now().plusMinutes(ttlMinutes));
        stringRedisTemplate.opsForValue().set(key, JsonUtils.toJson(redisData));
    }

    private boolean isExpired(RedisData<Spot> redisData) {
        return redisData.getExpireTime() == null || redisData.getExpireTime().isBefore(LocalDateTime.now());
    }
}
