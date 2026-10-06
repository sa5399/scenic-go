package com.scenicgo.reserve.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约单对外视图：列表只填摘要字段，详情再补核销码与几个时间。
 */
@Data
public class ReserveVO {

    private String orderNo;

    private String spotName;

    private LocalDate slotDate;

    private String slotStart;

    private String slotEnd;

    private Integer quantity;

    /** 单位：分 */
    private Integer totalAmount;

    private Integer status;

    private LocalDateTime createTime;

    /** 以下只有详情接口会填 */
    private String checkinCode;

    private LocalDateTime payTime;

    private LocalDateTime checkinTime;

    private LocalDateTime cancelTime;
}
