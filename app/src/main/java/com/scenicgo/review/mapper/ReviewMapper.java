package com.scenicgo.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scenicgo.review.entity.Review;
import com.scenicgo.review.vo.ReviewVO;
import org.apache.ibatis.annotations.Param;

public interface ReviewMapper extends BaseMapper<Review> {

    /** 某景点的评价分页，带昵称与头像，按时间倒序 */
    IPage<ReviewVO> selectReviewPage(IPage<ReviewVO> page, @Param("spotId") Long spotId);
}
