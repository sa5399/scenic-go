package com.scenicgo.admin.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运营订单查询的对外视图（比「我的预约」多下单人信息）。
 */
@Data
public class AdminReserveVO {

    private String orderNo;

    private Long userId;

    private String nickName;

    private Long spotId;

    private String spotName;

    private LocalDate slotDate;

    private String slotStart;

    private String slotEnd;

    private Integer quantity;

    /** 单位：分 */
    private Integer totalAmount;

    private Integer status;

    private LocalDateTime createTime;
}
