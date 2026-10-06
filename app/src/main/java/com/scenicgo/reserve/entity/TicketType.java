package com.scenicgo.reserve.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 票种字典（sys_ticket_type）。
 */
@Data
@TableName("sys_ticket_type")
public class TicketType {

    /** 主键是票种编码，不自动生成 */
    @TableId(value = "code", type = IdType.INPUT)
    private Integer code;

    private String name;

    /** 单位：分 */
    private Integer price;

    /** 每单限购张数 */
    private Integer limitPerSlot;
}
