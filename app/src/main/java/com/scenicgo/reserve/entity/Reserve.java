package com.scenicgo.reserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约单。
 *
 * <p>时段信息（日期 / 起止 / 票种 / 单价）是下单时的快照：库存表以后被改被删，
 * 订单也能还原出当时买的是什么。
 */
@Data
@TableName("reserve")
public class Reserve {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long spotId;

    private Long slotId;

    private LocalDate slotDate;

    private String slotStart;

    private String slotEnd;

    /** 1 成人 2 儿童 3 学生 */
    private Integer ticketType;

    private Integer quantity;

    /** 单位：分 */
    private Integer unitPrice;

    /** 单位：分 */
    private Integer totalAmount;

    /** 见 {@link com.scenicgo.reserve.constant.ReserveStatusEnum} */
    private Integer status;

    /** 支付成功后写入，NULL 表示还没有码 */
    private String checkinCode;

    private LocalDateTime payTime;

    private LocalDateTime checkinTime;

    private LocalDateTime cancelTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
