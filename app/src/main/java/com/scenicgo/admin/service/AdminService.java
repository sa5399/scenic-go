package com.scenicgo.admin.service;

import com.scenicgo.admin.dto.AdminReserveQuery;
import com.scenicgo.admin.dto.AdminSpotQuery;
import com.scenicgo.admin.vo.AdminReserveVO;
import com.scenicgo.admin.vo.AdminStatsVO;
import com.scenicgo.common.result.PageResult;
import com.scenicgo.spot.dto.AnnouncementFormDTO;
import com.scenicgo.spot.dto.SpotUpdateDTO;
import com.scenicgo.spot.vo.AnnouncementVO;
import com.scenicgo.spot.vo.SpotVO;

/**
 * 管理端操作：全部要求 `role = 3`（管理员），权限判断集中在这一个实现里。
 */
public interface AdminService {

    /** 景点上下架：1 上架 0 下架，改库后删详情缓存 */
    void updateSpotStatus(Long spotId, Integer status);

    /** 调整时段总库存，并同步 Redis 余量 */
    void adjustSlotStock(Long slotId, int totalStock);

    /** 新增公告，返回 id */
    Long createAnnouncement(AnnouncementFormDTO form);

    /** 修改公告标题与正文 */
    void updateAnnouncement(Long id, AnnouncementFormDTO form);

    /** 公告上下架：1 发布 0 下架 */
    void updateAnnouncementStatus(Long id, Integer status);

    /** 运营订单查询（全部用户），startDate/endDate 含当天 */
    PageResult<AdminReserveVO> listReserves(AdminReserveQuery query);

    /** 管理端景点列表（含下架） */
    PageResult<SpotVO> listSpotsForAdmin(AdminSpotQuery query);

    /** 编辑景点基础信息（名称 / 简介 / 价格） */
    void updateSpot(Long id, SpotUpdateDTO dto);

    /** 管理端公告列表（含下架），按创建时间倒序 */
    PageResult<AnnouncementVO> listAnnouncements(int current, int size, Integer status);

    /** 删除公告 */
    void deleteAnnouncement(Long id);

    /** 概览统计 */
    AdminStatsVO getStats();
}
