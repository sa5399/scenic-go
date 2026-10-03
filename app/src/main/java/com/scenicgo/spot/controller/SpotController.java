package com.scenicgo.spot.controller;

import com.scenicgo.common.result.PageResult;
import com.scenicgo.common.result.Result;
import com.scenicgo.spot.dto.SpotQuery;
import com.scenicgo.spot.service.SpotService;
import com.scenicgo.spot.vo.ExhibitionVO;
import com.scenicgo.spot.vo.SpotVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 景点 / 展馆查询接口。
 */
@Tag(name = "景点", description = "景点 / 展馆查询")
@RestController
@RequestMapping("/spots")
@RequiredArgsConstructor
public class SpotController {

    private final SpotService spotService;

    @Operation(summary = "景点列表（分页 + 关键字 + 类型）")
    @GetMapping
    public Result<PageResult<SpotVO>> list(@ParameterObject SpotQuery query) {
        return Result.ok(spotService.listSpots(query));
    }

    @Operation(summary = "景点详情")
    @GetMapping("/{id}")
    public Result<SpotVO> get(@PathVariable Long id) {
        return Result.ok(spotService.getSpot(id));
    }

    @Operation(summary = "景点下的展品")
    @GetMapping("/{id}/exhibitions")
    public Result<List<ExhibitionVO>> exhibitions(@PathVariable Long id) {
        return Result.ok(spotService.listExhibitions(id));
    }
}
