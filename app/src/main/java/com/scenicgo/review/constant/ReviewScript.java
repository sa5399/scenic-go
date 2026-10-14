package com.scenicgo.review.constant;

/**
 * 点赞的 Lua 脚本与返回值约定。
 *
 * <p>「成员在不在 Set 里」和「加进去还是拿出去」必须在一段 Lua 里做：
 * 拆成 Java 里的 SISMEMBER 再 SADD，同一用户并发点两下会两次都读到"没点过"。
 */
public class ReviewScript {

    /** 这次是取消点赞 */
    public static final long UNLIKED = 0L;

    /** 这次是点赞 */
    public static final long LIKED = 1L;

    /** KEYS[1]=review:liked:{id}，ARGV[1]=userId；返回 1 点赞 / 0 取消 */
    public static final String TOGGLE_LIKE = """
            if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
                redis.call('SREM', KEYS[1], ARGV[1])
                return 0
            end
            redis.call('SADD', KEYS[1], ARGV[1])
            return 1
            """;

    private ReviewScript() {
    }
}
