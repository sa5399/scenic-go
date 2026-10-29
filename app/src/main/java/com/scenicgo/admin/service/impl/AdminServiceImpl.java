package com.scenicgo.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenicgo.admin.dto.AdminReserveQuery;
import com.scenicgo.admin.mapper.AdminReserveMapper;
import com.scenicgo.admin.service.AdminService;
import com.scenicgo.admin.vo.AdminReserveVO;
import com.scenicgo.common.result.PageResult;
import com.scenicgo.reserve.service.StockService;
import com.scenicgo.spot.dto.AnnouncementFormDTO;
import com.scenicgo.spot.service.AnnouncementService;
import com.scenicgo.spot.service.SpotService;
import com.scenicgo.user.constant.UserRoleEnum;
import com.scenicgo.utils.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 管理端实现：只做权限判断 + 转发，具体业务留在各自模块的 service 里。
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SpotService spotService;

    private final StockService stockService;

    private final AnnouncementService announcementService;

    private final AdminReserveMapper adminReserveMapper;

    @Override
    public void updateSpotStatus(Long spotId, Integer status) {
        RoleGuard.require(UserRoleEnum.ADMIN);
        spotService.updateStatus(spotId, status);
    }

    @Override
    public void adjustSlotStock(Long slotId, int totalStock) {
        RoleGuard.require(UserRoleEnum.ADMIN);
        stockService.adjustTotalStock(slotId, totalStock);
    }

    @Override
    public Long createAnnouncement(AnnouncementFormDTO form) {
        RoleGuard.require(UserRoleEnum.ADMIN);
        return announcementService.create(form);
    }

    @Override
    public void updateAnnouncement(Long id, AnnouncementFormDTO form) {
        RoleGuard.require(UserRoleEnum.ADMIN);
        announcementService.update(id, form);
    }

    @Override
    public void updateAnnouncementStatus(Long id, Integer status) {
        RoleGuard.require(UserRoleEnum.ADMIN);
        announcementService.updateStatus(id, status);
    }

    @Override
    public PageResult<AdminReserveVO> listReserves(AdminReserveQuery query) {
        RoleGuard.require(UserRoleEnum.ADMIN);
        IPage<AdminReserveVO> page = adminReserveMapper.selectReservePage(
                Page.of(query.getCurrent(), query.getSize()), query.getSpotId(), query.getStatus());
        return PageResult.of(page);
    }
}
