package com.scenicgo.spot.controller;

import com.scenicgo.common.result.Result;
import com.scenicgo.spot.service.TagService;
import com.scenicgo.spot.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签字典接口。
 */
@Tag(name = "标签", description = "标签字典")
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "标签列表（可按类型筛选）")
    @GetMapping
    public Result<List<TagVO>> list(@RequestParam(required = false) Integer type) {
        return Result.ok(tagService.listTags(type));
    }
}
