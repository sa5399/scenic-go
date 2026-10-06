package com.scenicgo.reserve.constant;

import lombok.Getter;

/**
 * 时段可售状态（booking_slot.status）。
 */
@Getter
public enum SlotStatusEnum {

    SALABLE(1, "可售"),
    STOPPED(0, "停售");

    private final Integer code;
    private final String desc;

    SlotStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
