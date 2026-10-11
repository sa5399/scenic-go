package com.scenicgo.reserve.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 核销结果。
 */
@Data
@AllArgsConstructor
public class CheckinVO {

    private String orderNo;

    private String spotName;

    private Integer quantity;

    private LocalDateTime checkinTime;
}
