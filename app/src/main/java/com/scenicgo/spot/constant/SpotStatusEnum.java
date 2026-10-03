package com.scenicgo.spot.constant;

import lombok.Getter;

/**
 * 景点 / 展馆上下架状态（spot.status）。
 */
@Getter
public enum SpotStatusEnum {

    ON_SALE(1, "上架"),
    OFF_SALE(0, "下架");

    private final Integer code;
    private final String desc;

    SpotStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
