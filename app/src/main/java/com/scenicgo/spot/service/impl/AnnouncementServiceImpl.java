package com.scenicgo.spot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.ResultCode;
import com.scenicgo.spot.dto.AnnouncementFormDTO;
import com.scenicgo.spot.entity.Announcement;
import com.scenicgo.spot.mapper.AnnouncementMapper;
import com.scenicgo.spot.service.AnnouncementService;
import com.scenicgo.spot.vo.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告实现。
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private static final Integer PUBLISHED = 1;

    private static final Integer OFFLINE = 0;

    private final AnnouncementMapper announcementMapper;

    @Override
    public List<AnnouncementVO> listPublished() {
        List<Announcement> announcements = announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, PUBLISHED)
                .orderByDesc(Announcement::getCreateTime)
                .orderByDesc(Announcement::getId));
        return announcements.stream().map(AnnouncementVO::of).toList();
    }

    @Override
    public Long create(AnnouncementFormDTO form) {
        Announcement announcement = new Announcement();
        announcement.setTitle(form.getTitle());
        announcement.setContent(form.getContent());
        announcement.setStatus(PUBLISHED);
        announcementMapper.insert(announcement);
        return announcement.getId();
    }

    @Override
    public void update(Long id, AnnouncementFormDTO form) {
        getExisting(id);
        announcementMapper.update(null, new LambdaUpdateWrapper<Announcement>()
                .eq(Announcement::getId, id)
                .set(Announcement::getTitle, form.getTitle())
                .set(Announcement::getContent, form.getContent()));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (!PUBLISHED.equals(status) && !OFFLINE.equals(status)) {
            throw new BizException(ResultCode.BAD_REQUEST, "状态只能是 0 或 1");
        }
        getExisting(id);
        announcementMapper.update(null, new LambdaUpdateWrapper<Announcement>()
                .eq(Announcement::getId, id)
                .set(Announcement::getStatus, status));
    }

    private Announcement getExisting(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BizException(ResultCode.NOT_FOUND, "公告不存在");
        }
        return announcement;
    }
}
