package com.scenicgo.reserve.controller;

import com.scenicgo.common.result.PageResult;
import com.scenicgo.common.result.Result;
import com.scenicgo.reserve.dto.ReserveFormDTO;
import com.scenicgo.reserve.dto.ReserveQuery;
import com.scenicgo.reserve.service.ReserveService;
import com.scenicgo.reserve.vo.ReserveCreateVO;
import com.scenicgo.reserve.vo.ReservePayVO;
import com.scenicgo.reserve.vo.ReserveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(summary = "模拟支付")
    @PostMapping("/{orderNo}/pay")
    public Result<ReservePayVO> pay(@PathVariable String orderNo) {
        return Result.ok(reserveService.pay(orderNo));
    }

    @Operation(summary = "取消预约")
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        reserveService.cancel(orderNo);
        return Result.ok();
    }

    @Operation(summary = "我的预约（分页 + 状态筛选）")
    @GetMapping
    public Result<PageResult<ReserveVO>> list(@ParameterObject ReserveQuery query) {
        return Result.ok(reserveService.listReserves(query));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderNo}")
    public Result<ReserveVO> detail(@PathVariable String orderNo) {
        return Result.ok(reserveService.getReserve(orderNo));
    }
}
