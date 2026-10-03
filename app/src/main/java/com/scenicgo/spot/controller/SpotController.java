package com.scenicgo.spot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenicgo.common.result.Result;
import com.scenicgo.spot.service.SpotService;
import com.scenicgo.spot.vo.SpotVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 景点 / 展馆查询接口。
 */
@Tag(name = "景点", description = "景点 / 展馆查询")
@RestController
@RequestMapping("/spots")
@RequiredArgsConstructor
public class SpotController {

    private static final int MAX_PAGE_SIZE = 100;

    private final SpotService spotService;

    @Operation(summary = "景点列表（分页 + 关键字 + 类型）")
    @GetMapping
    public Result<Page<SpotVO>> list(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) Integer type,
                                     @RequestParam(defaultValue = "1") long page,
                                     @RequestParam(defaultValue = "10") long size) {
        return Result.ok(spotService.page(keyword, type,
                Math.max(page, 1),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE)));
    }

    @Operation(summary = "景点详情")
    @GetMapping("/{id}")
    public Result<SpotVO> detail(@PathVariable Long id) {
        return Result.ok(spotService.detail(id));
    }
}
