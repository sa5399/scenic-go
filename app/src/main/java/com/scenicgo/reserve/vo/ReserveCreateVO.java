package com.scenicgo.reserve.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 下单结果。
 */
@Data
@AllArgsConstructor
public class ReserveCreateVO {

    private String orderNo;

    /** 单位：分 */
    private Integer totalAmount;

    private Integer status;

    /** 待支付保留时长（分钟），超时自动关单 */
    private Integer expireMinutes;
}
