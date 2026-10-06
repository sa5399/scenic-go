package com.scenicgo.reserve.service;

import java.util.Collection;
import java.util.Map;

/**
 * 时段库存的 Redis 侧操作，读写两端都用它，保证 key 规则只有一处。
 */
public interface StockService {

    /** 批量读余量；返回的 Map 只含 Redis 里真实存在的 key */
    Map<Long, Integer> readStocks(Collection<Long> slotIds);

    /**
     * 把今天及以后的可售库存写进 Redis，余量 = total_stock - sold_stock。
     *
     * <p>启动时跑一次即可自愈：Redis 被清空、或上次进程在"扣了 Redis 还没落库"之间挂掉，
     * 都以 DB 为准重算回来。
     *
     * @return 预热的时段数
     */
    int preheat();

    /**
     * 原子预扣库存，失败直接抛业务异常（余票不足 / 张数不合法 / 库存未就绪）。
     *
     * <p>Redis 里没有这个 key 时按 DB 重建一次再试，所以预热漏掉的时段也能下单。
     */
    void deduct(Long slotId, int quantity);

    /**
     * 回补库存（取消、超时关单、下单失败的补偿）。
     *
     * <p>调用方要是先改了库再调它：key 不在时这里按 DB 重算，DB 是准的结论才算数。
     */
    void reback(Long slotId, int quantity);
}
