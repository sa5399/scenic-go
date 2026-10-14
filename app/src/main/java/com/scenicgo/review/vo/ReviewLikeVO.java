package com.scenicgo.review.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 点赞结果。
 */
@Data
@AllArgsConstructor
public class ReviewLikeVO {

    /** 当前点赞数 */
    private Integer liked;

    /** 这次点完，当前用户是否点了赞（false 表示这次是取消） */
    private Boolean likedByMe;
}
