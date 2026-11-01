package com.scenicgo.admin.dto;

import lombok.Data;

/**
 * 管理端景点列表查询条件（含下架景点）。
 */
@Data
public class AdminSpotQuery {

    /** 关键字，匹配名称或简介，不传表示不限 */
    private String keyword;

    /** 1 景点 / 0 展馆，不传表示不限 */
    private Integer type;

    /** 1 上架 / 0 下架，不传表示不限 */
    private Integer status;

    /** 页码，从 1 开始 */
    private int current = 1;

    /** 每页条数，上限由分页插件兜 */
    private int size = 10;
}
