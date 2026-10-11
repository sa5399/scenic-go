package com.scenicgo.reserve.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 核销流水对外视图。
 */
@Data
public class CheckinLogVO {

    private Long id;

    private String orderNo;

    /** 下单人 */
    private Long userId;

    private Long spotId;

    private String spotName;

    /** 核销员，0 表示闸机自动 */
    private Long operatorId;

    /** 1 闸机 2 人工 3 小程序自助 */
    private Integer channel;

    private LocalDateTime checkinTime;
}
