package com.scenicgo.admin.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 运营订单查询条件。
 */
@Data
public class AdminReserveQuery {

    /** 景点 id，不传表示不限 */
    private Long spotId;

    /** 订单状态，不传表示不限 */
    private Integer status;

    /** 页码，从 1 开始 */
    private int current = 1;

    /** 下单日期范围起点（yyyy-MM-dd），含当天，不传表示不限 */
    private LocalDate startDate;

    /** 下单日期范围终点（yyyy-MM-dd），含当天，不传表示不限 */
    private LocalDate endDate;

    /** 每页条数，上限由分页插件兜 */
    private int size = 10;
}
