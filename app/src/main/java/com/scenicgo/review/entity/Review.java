package com.scenicgo.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价。{@code content} 也是以后 AI 摘要的语料。
 */
@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long spotId;

    private Long userId;

    /** 关联的预约单，0 表示没关联（没预约也能评） */
    private Long reserveId;

    private String content;

    /** 1~5 */
    private Integer score;

    /** 点赞数：Redis Set 去重后回写，种子数据里有历史点赞数 */
    private Integer liked;

    /** 1 显示 0 隐藏 */
    private Integer status;

    private LocalDateTime createTime;
}
