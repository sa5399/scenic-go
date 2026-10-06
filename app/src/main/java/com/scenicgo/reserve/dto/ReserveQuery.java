package com.scenicgo.reserve.dto;

import lombok.Data;

/**
 * 我的预约查询条件。
 */
@Data
public class ReserveQuery {

    /** 0 待支付 1 已预约 2 已核销 3 已取消 4 已过期；不传表示不限 */
    private Integer status;

    /** 页码，从 1 开始 */
    private int current = 1;

    /** 每页条数，上限由分页插件兜 */
    private int size = 10;
}
