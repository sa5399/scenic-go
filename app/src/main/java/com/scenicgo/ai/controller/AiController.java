package com.scenicgo.ai.controller;

import com.scenicgo.ai.dto.AiRouteFormDTO;
import com.scenicgo.ai.service.AiRouteService;
import com.scenicgo.ai.vo.AiRouteVO;
import com.scenicgo.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 路线推荐接口。
 *
 * <p>第一版是**规则版**（标签 + 人群 + 时间预算 + 步行时长），不接大模型；
 * 向量化语义检索（原计划 7.1）与 AI 摘要在同一批暂缓。
 */
@Tag(name = "场景推荐", description = "规则版路线推荐")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiRouteService aiRouteService;

    @Operation(summary = "场景推荐路线（标签 + 人群 + 时间预算）")
    @PostMapping("/route")
    public Result<AiRouteVO> route(@RequestBody AiRouteFormDTO form) {
        return Result.ok(aiRouteService.planRoute(form));
    }
}
