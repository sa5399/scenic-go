package com.scenicgo.reserve.controller;

import com.scenicgo.common.result.Result;
import com.scenicgo.reserve.service.SlotService;
import com.scenicgo.reserve.vo.SlotVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 可预约时段接口。
 *
 * <p>路径挂在 {@code /spots/{id}} 下（时段属于某个景点），实现在 reserve 模块。
 */
@Tag(name = "预约时段", description = "可预约时段查询")
@RestController
@RequestMapping("/spots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    @Operation(summary = "可预约时段（按日期，默认今天）")
    @GetMapping("/{id}/slots")
    public Result<List<SlotVO>> slots(@PathVariable Long id,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(slotService.listSlots(id, date));
    }
}
