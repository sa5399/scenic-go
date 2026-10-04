package com.scenicgo.utils;

import com.scenicgo.auth.LoginUser;

/**
 * 当前登录用户，由拦截器写入、请求结束时清掉。
 *
 * <p>业务代码里用 {@link #get()} 拿用户，不用一层层往下传。
 */
public class UserHolder {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserHolder() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void remove() {
        HOLDER.remove();
    }
}
