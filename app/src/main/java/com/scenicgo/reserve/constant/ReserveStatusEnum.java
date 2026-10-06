package com.scenicgo.reserve.constant;

import lombok.Getter;

/**
 * 预约单状态（reserve.status）。
 *
 * <pre>
 * 0 待支付 ──支付──> 1 已预约 ──核销──> 2 已核销
 *    │                  │
 *    └──超时──> 4 已过期  └──取消──> 3 已取消
 * </pre>
 */
@Getter
public enum ReserveStatusEnum {

    PENDING(0, "待支付"),
    BOOKED(1, "已预约"),
    CHECKED_IN(2, "已核销"),
    CANCELED(3, "已取消"),
    EXPIRED(4, "已过期");

    private final Integer code;
    private final String desc;

    ReserveStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
