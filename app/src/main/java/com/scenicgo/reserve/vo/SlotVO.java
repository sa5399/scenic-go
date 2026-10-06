package com.scenicgo.reserve.vo;

import lombok.Data;

/**
 * 可预约时段对外视图。{@code remaining} 取自 Redis，Redis 里没有才回落到库里算的余量。
 */
@Data
public class SlotVO {

    private Long slotId;

    private String slotStart;

    private String slotEnd;

    /** 1 成人 2 儿童 3 学生 */
    private Integer ticketType;

    private String ticketName;

    /** 单位：分 */
    private Integer price;

    private Integer remaining;

    /** 每单限购张数 */
    private Integer limitPerSlot;
}
