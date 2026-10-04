package com.scenicgo.guide.constant;

import lombok.Getter;

/**
 * 设施状态（facility.status）。
 */
@Getter
public enum FacilityStatusEnum {

    ENABLED(1, "可用"),
    DISABLED(0, "停用");

    private final Integer code;
    private final String desc;

    FacilityStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
