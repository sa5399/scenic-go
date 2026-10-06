package com.scenicgo.reserve.controller;

import com.scenicgo.common.result.Result;
import com.scenicgo.reserve.dto.ReserveFormDTO;
import com.scenicgo.reserve.service.ReserveService;
import com.scenicgo.reserve.vo.ReserveCreateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预约订单接口。
 */
@Tag(name = "预约订单", description = "下单 / 支付 / 取消 / 查询")
@RestController
@RequestMapping("/reserves")
@RequiredArgsConstructor
public class ReserveController {

    private final ReserveService reserveService;

    @Operation(summary = "下单（Lua 原子预扣库存）")
    @PostMapping
    public Result<ReserveCreateVO> create(@RequestBody ReserveFormDTO form) {
        return Result.ok(reserveService.create(form));
    }
}
