package com.scenicgo.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.PageResult;
import com.scenicgo.common.result.ResultCode;
import com.scenicgo.reserve.dto.ReserveBrief;
import com.scenicgo.reserve.service.ReserveService;
import com.scenicgo.review.constant.ReviewScript;
import com.scenicgo.review.dto.ReviewFormDTO;
import com.scenicgo.review.dto.ReviewQuery;
import com.scenicgo.review.entity.Review;
import com.scenicgo.review.mapper.ReviewMapper;
import com.scenicgo.review.service.ReviewService;
import com.scenicgo.review.vo.ReviewLikeVO;
import com.scenicgo.review.vo.ReviewVO;
import com.scenicgo.utils.RedisConstants;
import com.scenicgo.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评价实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final DefaultRedisScript<Long> TOGGLE_LIKE_SCRIPT =
            new DefaultRedisScript<>(ReviewScript.TOGGLE_LIKE, Long.class);

    private final ReviewMapper reviewMapper;

    private final ReserveService reserveService;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Long create(ReviewFormDTO form) {
        // 评分不校验的话，平均分这类统计会被一条 0 分或 99 分污染，
        // 这里破例拦一道，别的入参仍然按"后端不写校验分支"走
        if (form.getScore() < 1 || form.getScore() > 5) {
            throw new BizException(ResultCode.BAD_REQUEST, "评分只能是 1~5");
        }
        // 表里 reserve_id 非空、0 表示没关联
        Long reserveId = form.getReserveId() == null ? 0L : form.getReserveId();
        if (reserveId > 0) {
            ReserveBrief reserve = reserveService.getMineCheckedIn(reserveId);
            if (!reserve.getSpotId().equals(form.getSpotId())) {
                throw new BizException("订单和景点对不上，不能关联");
            }
        }

        Review review = new Review();
        review.setSpotId(form.getSpotId());
        review.setUserId(UserHolder.get().getId());
        review.setReserveId(reserveId);
        review.setContent(form.getContent());
        review.setScore(form.getScore());
        // liked / status 不设，走表默认值（0 / 1）
        reviewMapper.insert(review);

        log.info("发表评价 id={} spotId={} userId={} reserveId={} score={}",
                review.getId(), review.getSpotId(), review.getUserId(), reserveId, review.getScore());
        return review.getId();
    }

    @Override
    public PageResult<ReviewVO> listBySpot(Long spotId, ReviewQuery query) {
        IPage<ReviewVO> page = reviewMapper.selectReviewPage(
                Page.of(query.getCurrent(), query.getSize()), spotId);
        return PageResult.of(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewLikeVO toggleLike(Long reviewId) {
        Review review = reviewMapper.selectOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getId, reviewId)
                .select(Review::getId, Review::getLiked));
        if (review == null) {
            throw new BizException(ResultCode.NOT_FOUND, "评价不存在");
        }
        Long userId = UserHolder.get().getId();

        // Set 里有没有我，决定这次是赞还是取消；重复点不会重复计数
        Long toggled = stringRedisTemplate.execute(TOGGLE_LIKE_SCRIPT,
                List.of(RedisConstants.REVIEW_LIKED_KEY + reviewId), String.valueOf(userId));
        boolean liked = toggled != null && toggled == ReviewScript.LIKED;

        reviewMapper.update(null, new LambdaUpdateWrapper<Review>()
                .eq(Review::getId, reviewId)
                .setSql(liked ? "liked = liked + 1" : "liked = liked - 1")
                // 取消时兜住 0：种子里的历史点赞数没有对应的 Set 记录，减不到负数
                .ge(!liked, Review::getLiked, 1));

        Integer current = reviewMapper.selectOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getId, reviewId)
                .select(Review::getLiked)).getLiked();
        log.info("点赞变更 reviewId={} userId={} liked={} likedByMe={}", reviewId, userId, current, liked);
        return new ReviewLikeVO(current, liked);
    }
}
