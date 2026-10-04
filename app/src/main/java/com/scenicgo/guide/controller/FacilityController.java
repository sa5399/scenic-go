package com.scenicgo.guide.controller;

import com.scenicgo.common.result.Result;
import com.scenicgo.guide.dto.FacilityQuery;
import com.scenicgo.guide.service.FacilityService;
import com.scenicgo.guide.vo.FacilityVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 园区设施接口。
 */
@Tag(name = "设施", description = "园区设施")
@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @Operation(summary = "附近设施（按距离升序）")
    @GetMapping("/nearby")
    public Result<List<FacilityVO>> nearby(@ParameterObject FacilityQuery query) {
        return Result.ok(facilityService.listNearby(query));
    }
}
