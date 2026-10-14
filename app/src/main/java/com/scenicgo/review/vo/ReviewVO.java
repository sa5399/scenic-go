package com.scenicgo.review.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价对外视图。
 */
@Data
public class ReviewVO {

    private Long id;

    private Long userId;

    private String nickName;

    private String icon;

    private String content;

    /** 1~5 */
    private Integer score;

    /** 点赞数 */
    private Integer liked;

    private LocalDateTime createTime;
}
