package com.scenicgo.reserve.constant;

/**
 * 库存 Lua 脚本与它的返回值约定。
 *
 * <p>「检查再扣减」必须在一段 Lua 里做：Redis 执行脚本期间不插别的命令，
 * 所以不会出现两个请求都读到"还剩 1 张"然后各扣 1 张的超卖。
 */
public class StockScript {

    /** 余票不足 */
    public static final long NOT_ENOUGH = -1L;

    /** key 不在 Redis 里（没预热过或被清空），调用方按 DB 重建后再试 */
    public static final long KEY_MISSING = -2L;

    /** 张数不合法（≤0） */
    public static final long BAD_QUANTITY = -3L;

    /** KEYS[1]=slot:stock:{id}，ARGV[1]=张数；返回扣减后的余量 */
    public static final String DEDUCT = """
            local stock = redis.call('GET', KEYS[1])
            if not stock then
                return -2
            end
            local quantity = tonumber(ARGV[1])
            if quantity < 1 then
                return -3
            end
            if tonumber(stock) < quantity then
                return -1
            end
            return redis.call('DECRBY', KEYS[1], quantity)
            """;

    /**
     * KEYS[1]=slot:stock:{id}，ARGV[1]=张数；返回回补后的余量。
     *
     * <p>先判 key 在不在：不在就返回 {@link #KEY_MISSING} 交给调用方按 DB 重算，
     * 免得给一个已经没人读的老时段凭空建 key。
     */
    public static final String REBACK = """
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return -2
            end
            return redis.call('INCRBY', KEYS[1], ARGV[1])
            """;

    private StockScript() {
    }
}
