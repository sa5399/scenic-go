package com.scenicgo.review.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价对外视图。
 */
@Data
public class ReviewVO {

    private Long id;

    /** 以下两个字段只有「我的评价」会填：景点页列表的调用方本来就已知景点 */
    private Long spotId;

    private String spotName;

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
