package com.scenicgo.spot.dto;

import lombok.Data;

import java.util.List;

/**
 * 景点列表查询条件。
 */
@Data
public class SpotQuery {

    /** 关键字，匹配名称或简介 */
    private String keyword;

    /** 1 景点 / 0 展馆，不传表示不限 */
    private Integer type;

    /** 标签 id，逗号分隔或重复传参；多个是「全部命中」 */
    private List<Long> tagIds;

    /** 页码，从 1 开始 */
    private int current = 1;

    /** 每页条数，上限由分页插件兜 */
    private int size = 10;
}
