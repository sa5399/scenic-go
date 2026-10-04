package com.scenicgo.user.constant;

import lombok.Getter;

/**
 * 用户状态（user.status）。
 */
@Getter
public enum UserStatusEnum {

    NORMAL(1, "正常"),
    BANNED(0, "封禁");

    private final Integer code;
    private final String desc;

    UserStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
