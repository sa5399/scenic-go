package com.scenicgo.spot.vo;

import com.scenicgo.spot.entity.Announcement;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * 公告对外视图。
 */
@Data
public class AnnouncementVO {

    private Long id;

    private String title;

    private String content;

    /** 1 发布 0 下架 */
    private Integer status;

    private LocalDateTime createTime;

    public static AnnouncementVO of(Announcement announcement) {
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtils.copyProperties(announcement, vo);
        return vo;
    }
}
