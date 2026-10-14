package com.scenicgo.review.dto;

import lombok.Data;

/**
 * 评价列表查询条件。
 */
@Data
public class ReviewQuery {

    /** 页码，从 1 开始 */
    private int current = 1;

    /** 每页条数，上限由分页插件兜 */
    private int size = 10;
}
