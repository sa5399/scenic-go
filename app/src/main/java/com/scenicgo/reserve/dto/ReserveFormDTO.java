package com.scenicgo.reserve.dto;

import lombok.Data;

/**
 * 下单入参。
 */
@Data
public class ReserveFormDTO {

    private Long slotId;

    /** 张数，不传按 1 张；传 0 或负数会被库存脚本挡下来 */
    private int quantity = 1;
}
