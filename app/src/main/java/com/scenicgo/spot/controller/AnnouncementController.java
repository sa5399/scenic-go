package com.scenicgo.spot.controller;

import com.scenicgo.common.result.Result;
import com.scenicgo.spot.service.AnnouncementService;
import com.scenicgo.spot.vo.AnnouncementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公告查询（公开）。维护类接口在管理端 `/admin/announcements`。
 */
@Tag(name = "公告", description = "公告浏览")
@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "公告列表（只返回已发布，时间倒序）")
    @GetMapping
    public Result<List<AnnouncementVO>> list() {
        return Result.ok(announcementService.listPublished());
    }
}
