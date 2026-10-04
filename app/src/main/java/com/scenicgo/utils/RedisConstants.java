package com.scenicgo.utils;

public class RedisConstants {

    /** 设施坐标，member 是 facility.id */
    public static final String GEO_FACILITY_KEY = "geo:facility";

    /** 登录 token，value 是登录用户 JSON；每次访问续期 */
    public static final String LOGIN_TOKEN_KEY = "login:token:";
    public static final Long LOGIN_TOKEN_TTL_MINUTES = 30L;
}
