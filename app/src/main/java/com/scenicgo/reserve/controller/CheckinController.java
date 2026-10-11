package com.scenicgo.reserve.controller;

import com.scenicgo.common.result.PageResult;
import com.scenicgo.common.result.Result;
import com.scenicgo.reserve.dto.CheckinFormDTO;
import com.scenicgo.reserve.dto.CheckinLogQuery;
import com.scenicgo.reserve.service.CheckinService;
import com.scenicgo.reserve.vo.CheckinLogVO;
import com.scenicgo.reserve.vo.CheckinVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 核销接口。角色在 service 里判：核销员或管理员可核销，只有管理员能查流水。
 */
@Tag(name = "核销", description = "核销与核销流水")
@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @Operation(summary = "核销（核销员 / 管理员）")
    @PostMapping
    public Result<CheckinVO> checkin(@RequestBody CheckinFormDTO form) {
        return Result.ok(checkinService.checkin(form));
    }

    @Operation(summary = "核销流水（管理员）")
    @GetMapping("/logs")
    public Result<PageResult<CheckinLogVO>> logs(@ParameterObject CheckinLogQuery query) {
        return Result.ok(checkinService.listLogs(query));
    }
}
