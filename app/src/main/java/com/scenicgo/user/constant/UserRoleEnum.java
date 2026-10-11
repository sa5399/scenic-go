package com.scenicgo.user.constant;

import lombok.Getter;

/**
 * 用户角色（user.role）。
 */
@Getter
public enum UserRoleEnum {

    TOURIST(0, "游客"),
    MEMBER(1, "会员"),
    CHECKIN_STAFF(2, "核销员"),
    ADMIN(3, "管理员");

    private final Integer code;
    private final String desc;

    UserRoleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
