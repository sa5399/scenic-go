package com.scenicgo.admin.controller;

import com.scenicgo.admin.dto.AdminReserveQuery;
import com.scenicgo.admin.service.AdminService;
import com.scenicgo.admin.vo.AdminReserveVO;
import com.scenicgo.common.result.PageResult;
import com.scenicgo.common.result.Result;
import com.scenicgo.spot.dto.AnnouncementFormDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端接口（`role = 3` 管理员）。
 *
 * <p>路径横跨 `/admin/spots`、`/admin/slots`、`/admin/announcements`、`/admin/reserves`，
 * 所以不挂类级 {@code @RequestMapping}，每条写全；都不在免登录名单里。
 */
@Tag(name = "管理端", description = "景点上下架 / 库存调整 / 公告维护 / 订单查询")
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "景点上下架（1 上架 0 下架）")
    @PutMapping("/admin/spots/{id}/status")
    public Result<Void> updateSpotStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.updateSpotStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "调整时段总库存")
    @PutMapping("/admin/slots/{id}/stock")
    public Result<Void> adjustSlotStock(@PathVariable Long id, @RequestParam int totalStock) {
        adminService.adjustSlotStock(id, totalStock);
        return Result.ok();
    }

    @Operation(summary = "新增公告")
    @PostMapping("/admin/announcements")
    public Result<Long> createAnnouncement(@RequestBody AnnouncementFormDTO form) {
        return Result.ok(adminService.createAnnouncement(form));
    }

    @Operation(summary = "修改公告")
    @PutMapping("/admin/announcements/{id}")
    public Result<Void> updateAnnouncement(@PathVariable Long id, @RequestBody AnnouncementFormDTO form) {
        adminService.updateAnnouncement(id, form);
        return Result.ok();
    }

    @Operation(summary = "公告上下架（1 发布 0 下架）")
    @PutMapping("/admin/announcements/{id}/status")
    public Result<Void> updateAnnouncementStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.updateAnnouncementStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "运营订单查询（全部用户）")
    @GetMapping("/admin/reserves")
    public Result<PageResult<AdminReserveVO>> listReserves(@ParameterObject AdminReserveQuery query) {
        return Result.ok(adminService.listReserves(query));
    }
}
