package com.scenicgo.reserve.service.impl;

import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.ResultCode;
import com.scenicgo.reserve.constant.StockScript;
import com.scenicgo.reserve.entity.BookingSlot;
import com.scenicgo.reserve.mapper.BookingSlotMapper;
import com.scenicgo.reserve.service.StockService;
import com.scenicgo.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 时段库存的 Redis 实现。库存以 Redis 为准，DB 只记账与对账。
 */
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private static final DefaultRedisScript<Long> DEDUCT_SCRIPT =
            new DefaultRedisScript<>(StockScript.DEDUCT, Long.class);

    private static final DefaultRedisScript<Long> REBACK_SCRIPT =
            new DefaultRedisScript<>(StockScript.REBACK, Long.class);

    /** 重建锁最长持有时长，防止线程挂掉后锁不释放 */
    private static final long LOCK_LEASE_SECONDS = 10L;

    private final StringRedisTemplate stringRedisTemplate;

    private final BookingSlotMapper bookingSlotMapper;

    private final RedissonClient redissonClient;

    @Override
    public Map<Long, Integer> readStocks(Collection<Long> slotIds) {
        if (slotIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = List.copyOf(slotIds);
        List<String> keys = ids.stream().map(id -> RedisConstants.SLOT_STOCK_KEY + id).toList();
        List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);

        Map<Long, Integer> stocks = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            // multiGet 命中不到的位置是 null，正好当"这个 key 不在 Redis 里"用
            String value = values == null ? null : values.get(i);
            if (value != null) {
                stocks.put(ids.get(i), Integer.valueOf(value));
            }
        }
        return stocks;
    }

    @Override
    public int preheat() {
        List<BookingSlot> slots = bookingSlotMapper.selectPreheatSlots(LocalDate.now());
        if (slots.isEmpty()) {
            return 0;
        }
        Map<String, String> stockByKey = new HashMap<>(slots.size());
        for (BookingSlot slot : slots) {
            stockByKey.put(RedisConstants.SLOT_STOCK_KEY + slot.getId(),
                    String.valueOf(slot.getTotalStock() - slot.getSoldStock()));
        }
        // 一次 MSET 写完，不设 TTL：老时段的 key 没人再读，重新预热会覆盖
        stringRedisTemplate.opsForValue().multiSet(stockByKey);
        return slots.size();
    }

    @Override
    public void deduct(Long slotId, int quantity) {
        Long result = executeDeduct(slotId, quantity);
        if (result != null && result == StockScript.KEY_MISSING) {
            rebuildFromDb(slotId);
            result = executeDeduct(slotId, quantity);
        }
        if (result == null) {
            throw new BizException("库存未就绪，请稍后重试");
        }
        if (result == StockScript.BAD_QUANTITY) {
            throw new BizException(ResultCode.BAD_REQUEST, "预约张数不合法");
        }
        if (result == StockScript.NOT_ENOUGH) {
            throw new BizException("余票不足");
        }
    }

    @Override
    public void reback(Long slotId, int quantity) {
        // key 不在时脚本直接跳过：DB 已经改完，下次扣减会按 DB 重建余量
        stringRedisTemplate.execute(REBACK_SCRIPT,
                List.of(RedisConstants.SLOT_STOCK_KEY + slotId), String.valueOf(quantity));
    }

    private Long executeDeduct(Long slotId, int quantity) {
        return stringRedisTemplate.execute(DEDUCT_SCRIPT,
                List.of(RedisConstants.SLOT_STOCK_KEY + slotId), String.valueOf(quantity));
    }

    /**
     * 按 DB 重算余量写回 Redis（total_stock - sold_stock）。
     *
     * <p>Redis 被清空、key 过期、或进程在"扣了 Redis 还没落库"之间挂掉，都靠这一步回到一致。
     */
    private void rebuildFromDb(Long slotId) {
        String key = RedisConstants.SLOT_STOCK_KEY + slotId;
        RLock lock = redissonClient.getLock(RedisConstants.LOCK_SLOT_KEY + slotId);
        try {
            // 等一小会儿：可能别人正在重建，等不到就自己来
            if (!lock.tryLock(1L, LOCK_LEASE_SECONDS, TimeUnit.SECONDS)) {
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        try {
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
                return;
            }
            BookingSlot slot = bookingSlotMapper.selectById(slotId);
            if (slot == null) {
                return;
            }
            stringRedisTemplate.opsForValue()
                    .set(key, String.valueOf(slot.getTotalStock() - slot.getSoldStock()));
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
