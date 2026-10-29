package com.scenicgo.spot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告（临时闭园、限流提示这类）。
 */
@Data
@TableName("announcement")
public class Announcement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /** TEXT */
    private String content;

    /** 1 发布 0 下架 */
    private Integer status;

    private LocalDateTime createTime;
}
