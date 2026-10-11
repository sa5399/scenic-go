package com.scenicgo.reserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 核销流水。{@code reserve_id} 上有唯一键，一张票只可能有一条。
 */
@Data
@TableName("checkin_log")
public class CheckinLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reserveId;

    private String orderNo;

    /** 下单人，不是核销员 */
    private Long userId;

    private Long spotId;

    /** 核销员，0 表示闸机自动 */
    private Long operatorId;

    /** 1 闸机 2 人工 3 小程序自助 */
    private Integer channel;

    private LocalDateTime checkinTime;
}
