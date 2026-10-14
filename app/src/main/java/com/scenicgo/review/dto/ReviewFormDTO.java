package com.scenicgo.review.dto;

import lombok.Data;

/**
 * 发表评价入参。
 */
@Data
public class ReviewFormDTO {

    private Long spotId;

    /** 关联的预约单 id，可传 null；传了就必须是自己的、已核销的、且景点一致 */
    private Long reserveId;

    private String content;

    /** 1~5，不传按 5 分 */
    private int score = 5;
}
