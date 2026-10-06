package com.scenicgo.reserve.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付结果。
 */
@Data
@AllArgsConstructor
public class ReservePayVO {

    private String orderNo;

    private Integer status;

    /** 核销码，进场时用它 */
    private String checkinCode;

    private LocalDateTime payTime;
}
