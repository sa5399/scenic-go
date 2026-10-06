package com.scenicgo.reserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 时段库存：景点 × 日期 × 时段 × 票种。
 *
 * <p>真正的余量在 Redis（{@code slot:stock:{id}}），这张表记录与对账。
 */
@Data
@TableName("booking_slot")
public class BookingSlot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long spotId;

    private LocalDate slotDate;

    /** 形如 08:00，定长所以字符串比较等价于时间比较 */
    private String slotStart;

    private String slotEnd;

    /** 1 成人 2 儿童 3 学生 */
    private Integer ticketType;

    /** 单位：分 */
    private Integer price;

    private Integer totalStock;

    /** 已占用（含待支付）；余量 = totalStock - soldStock */
    private Integer soldStock;

    /** 1 可售 0 停售 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
