package com.scenicgo.review.service;

import com.scenicgo.common.result.PageResult;
import com.scenicgo.review.dto.ReviewFormDTO;
import com.scenicgo.review.dto.ReviewQuery;
import com.scenicgo.review.vo.ReviewLikeVO;
import com.scenicgo.review.vo.ReviewVO;

public interface ReviewService {

    /** 发表评价，返回评价 id */
    Long create(ReviewFormDTO form);

    /** 某景点的评价分页（按时间倒序） */
    PageResult<ReviewVO> listBySpot(Long spotId, ReviewQuery query);

    /** 点赞 / 取消点赞：Redis Set 去重，同一用户重复点不会重复计数 */
    ReviewLikeVO toggleLike(Long reviewId);
}
