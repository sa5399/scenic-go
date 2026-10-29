package com.scenicgo.spot.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.scenicgo.common.exception.BizException;
import com.scenicgo.spot.dto.SpotDetail;
import com.scenicgo.spot.entity.Spot;
import com.scenicgo.spot.mapper.SpotMapper;
import com.scenicgo.spot.mapper.TagMapper;
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
 * <p>缓存的是「详情页要用的整份数据」（景点 + 标签），命中时一次库都不查；
 * key 不设物理 TTL，数据里带 {@code expireTime}，逻辑过期时返回旧值并由后台线程重建。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpotCacheServiceImpl implements SpotCacheService {

    private static final TypeReference<RedisData<SpotDetail>> CACHE_TYPE =
            new TypeReference<RedisData<SpotDetail>>() {
            };

    /** 重建锁最长持有时长，防止线程挂掉后锁不释放 */
    private static final long LOCK_LEASE_SECONDS = 10L;

    private final StringRedisTemplate stringRedisTemplate;

    private final SpotMapper spotMapper;

    private final TagMapper tagMapper;

    private final RedissonClient redissonClient;

    private final ThreadPoolTaskExecutor cacheRebuildExecutor;

    @Override
    public SpotDetail getSpotDetail(Long id) {
        String key = RedisConstants.CACHE_SPOT_KEY + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return loadOnColdStart(id, key);
        }

        RedisData<SpotDetail> cached = JsonUtils.fromJson(json, CACHE_TYPE);
        if (isExpired(cached)) {
            asyncRebuild(id, key);
        }
        return cached.getData();
    }

    /** 缓存里什么都没有：抢锁串行查库，抢不到就等一小会儿再读，仍没有就直接查库兜底 */
    private SpotDetail loadOnColdStart(Long id, String key) {
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
                return loadFromDb(id);
            }
            // 双检：等锁期间别人可能已经写好了
            String current = stringRedisTemplate.opsForValue().get(key);
            if (current != null) {
                return JsonUtils.fromJson(current, CACHE_TYPE).getData();
            }
            SpotDetail detail = loadFromDb(id);
            writeCache(key, detail);
            return detail;
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
                writeCache(key, loadFromDb(id));
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

    @Override
    public void evict(Long id) {
        stringRedisTemplate.delete(RedisConstants.CACHE_SPOT_KEY + id);
    }

    /** 一次把详情要用的数据查全：景点不存在就返回 null（下面按空值缓存） */
    private SpotDetail loadFromDb(Long id) {
        Spot spot = spotMapper.selectById(id);
        if (spot == null) {
            return null;
        }
        return new SpotDetail(spot, tagMapper.selectBySpotId(id));
    }

    /** 查不到也写缓存（防穿透），只是逻辑过期时间短得多；不设物理 TTL */
    private void writeCache(String key, SpotDetail detail) {
        long ttlMinutes = detail == null
                ? RedisConstants.CACHE_NULL_TTL_MINUTES
                : RedisConstants.CACHE_SPOT_TTL_MINUTES;
        RedisData<SpotDetail> redisData = new RedisData<>(detail, LocalDateTime.now().plusMinutes(ttlMinutes));
        stringRedisTemplate.opsForValue().set(key, JsonUtils.toJson(redisData));
    }

    private boolean isExpired(RedisData<SpotDetail> redisData) {
        return redisData.getExpireTime() == null || redisData.getExpireTime().isBefore(LocalDateTime.now());
    }
}
