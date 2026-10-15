package com.scenicgo.guide.controller;

import com.scenicgo.common.result.Result;
import com.scenicgo.guide.dto.RouteFormDTO;
import com.scenicgo.guide.service.RouteService;
import com.scenicgo.guide.vo.RouteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 推荐路线接口。
 *
 * <p>纯规则：标签 + 人群 + 时间预算 + 步行时长，不依赖任何模型服务。
 */
@Tag(name = "推荐路线", description = "按标签 / 人群 / 时间预算出路线")
@RestController
@RequestMapping("/guide")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @Operation(summary = "推荐路线（标签 + 人群 + 时间预算）")
    @PostMapping("/route")
    public Result<RouteVO> route(@RequestBody RouteFormDTO form) {
        return Result.ok(routeService.planRoute(form));
    }
}
