package com.scenicgo.reserve.dto;

import lombok.Data;

/**
 * 预约单的简要信息，给跨模块读用（比如发表评价时校验订单）。
 *
 * <p>不直接把 entity 递出去：entity 只在 service 内部流转。
 */
@Data
public class ReserveBrief {

    private Long id;

    private Long spotId;

    private Long userId;

    /** 见 {@link com.scenicgo.reserve.constant.ReserveStatusEnum} */
    private Integer status;
}
