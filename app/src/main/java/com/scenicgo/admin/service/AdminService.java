package com.scenicgo.admin.service;

import com.scenicgo.admin.dto.AdminReserveQuery;
import com.scenicgo.admin.vo.AdminReserveVO;
import com.scenicgo.common.result.PageResult;
import com.scenicgo.spot.dto.AnnouncementFormDTO;

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

    /** 运营订单查询（全部用户） */
    PageResult<AdminReserveVO> listReserves(AdminReserveQuery query);
}
