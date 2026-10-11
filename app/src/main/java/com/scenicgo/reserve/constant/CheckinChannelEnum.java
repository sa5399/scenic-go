package com.scenicgo.reserve.constant;

import lombok.Getter;

/**
 * 核销渠道（checkin_log.channel）。
 */
@Getter
public enum CheckinChannelEnum {

    GATE(1, "闸机"),
    MANUAL(2, "人工"),
    SELF_SERVICE(3, "小程序自助");

    private final Integer code;
    private final String desc;

    CheckinChannelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
