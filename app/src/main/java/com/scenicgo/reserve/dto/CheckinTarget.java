package com.scenicgo.reserve.dto;

import lombok.Data;

/**
 * 核销时按核销码查出来的目标：既要判状态与归属，也要凑出核销流水的字段，
 * 所以一次查全（含景点名），不对外返回。
 */
@Data
public class CheckinTarget {

    private Long id;

    /** 见 {@link com.scenicgo.reserve.constant.ReserveStatusEnum} */
    private Integer status;

    private String orderNo;

    /** 下单人，写流水用 */
    private Long userId;

    private Long spotId;

    private Integer quantity;

    private String spotName;
}
