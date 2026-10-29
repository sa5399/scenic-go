package com.scenicgo.spot.dto;

import lombok.Data;

/**
 * 公告新增 / 修改入参（管理端）。
 */
@Data
public class AnnouncementFormDTO {

    private String title;

    private String content;
}
