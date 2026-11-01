package com.scenicgo.admin.vo;

import lombok.Data;

/**
 * 概览统计（一次查询出的五个计数）。
 */
@Data
public class AdminStatsVO {

    /** 在架景点 / 展馆数 */
    private int spotOnSale;

    /** 下架景点 / 展馆数 */
    private int spotOffline;

    /** 累计核销笔数 */
    private int checkinTotal;

    /** 今日核销笔数 */
    private int checkinToday;

    /** 待支付订单数 */
    private int reservePending;
}
