package com.scenicgo.spot.service;

import com.scenicgo.spot.dto.AnnouncementFormDTO;
import com.scenicgo.spot.vo.AnnouncementVO;

import java.util.List;

public interface AnnouncementService {

    /** 已发布的公告，按发布（创建）时间倒序 */
    List<AnnouncementVO> listPublished();

    /** 新增公告（管理端），返回 id */
    Long create(AnnouncementFormDTO form);

    /** 修改标题与正文（管理端） */
    void update(Long id, AnnouncementFormDTO form);

    /** 上下架（管理端）：1 发布 0 下架 */
    void updateStatus(Long id, Integer status);
}
