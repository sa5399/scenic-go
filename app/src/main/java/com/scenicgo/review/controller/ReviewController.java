package com.scenicgo.review.controller;

import com.scenicgo.common.result.PageResult;
import com.scenicgo.common.result.Result;
import com.scenicgo.review.dto.ReviewFormDTO;
import com.scenicgo.review.dto.ReviewQuery;
import com.scenicgo.review.service.ReviewService;
import com.scenicgo.review.vo.ReviewLikeVO;
import com.scenicgo.review.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评价接口。
 *
 * <p>路径横跨 `/reviews` 与 `/spots/{id}/reviews` 两个资源，所以不挂类级 {@code @RequestMapping}，
 * 每条各自写全（`/spots/**` 在免登录名单里，列表接口因此免登录）。
 */
@Tag(name = "评价", description = "发表 / 列表 / 点赞")
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "发表评价")
    @PostMapping("/reviews")
    public Result<Long> create(@RequestBody ReviewFormDTO form) {
        return Result.ok(reviewService.create(form));
    }

    @Operation(summary = "景点评价列表（分页，按时间倒序）")
    @GetMapping("/spots/{id}/reviews")
    public Result<PageResult<ReviewVO>> listBySpot(@PathVariable Long id,
                                                   @ParameterObject ReviewQuery query) {
        return Result.ok(reviewService.listBySpot(id, query));
    }

    @Operation(summary = "点赞 / 取消点赞")
    @PostMapping("/reviews/{id}/like")
    public Result<ReviewLikeVO> like(@PathVariable Long id) {
        return Result.ok(reviewService.toggleLike(id));
    }
}
