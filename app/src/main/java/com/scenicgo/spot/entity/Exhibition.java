package com.scenicgo.spot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 展品。
 */
@Data
@TableName("exhibition")
public class Exhibition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long spotId;

    private String name;

    /** 类别，如 陶瓷 / 书画 */
    private String category;

    private String summary;

    /** 讲解词，TEXT，AI 语料 */
    private String narrative;

    private String coverImg;

    /** 馆内排序 */
    private Integer sort;

    /** 1 上架 0 下架 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
