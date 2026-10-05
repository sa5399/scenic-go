package com.scenicgo.utils;

public class RedisConstants {

    /** 设施坐标，member 是 facility.id */
    public static final String GEO_FACILITY_KEY = "geo:facility";

    /** 登录 token，value 是登录用户 JSON；每次访问续期 */
    public static final String LOGIN_TOKEN_KEY = "login:token:";
    public static final Long LOGIN_TOKEN_TTL_MINUTES = 30L;

    /** 景点缓存，value 是 RedisData<Spot> JSON；不设物理 TTL，靠里面的 expireTime */
    public static final String CACHE_SPOT_KEY = "cache:spot:";
    public static final Long CACHE_SPOT_TTL_MINUTES = 30L;
    /** 查不到的也缓存，但逻辑过期短得多 */
    public static final Long CACHE_NULL_TTL_MINUTES = 2L;
    /** 热点 key 重建用的锁 */
    public static final String LOCK_SPOT_KEY = "lock:spot:";
}
