package com.scenicgo.spot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 景点 / 展馆（type：1 景点，0 展馆）。
 */
@Data
@TableName("spot")
public class Spot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 一句话简介，列表页用 */
    private String summary;

    /** 详细介绍，详情页用（TEXT，列表查询不要带出来） */
    private String content;

    private String coverImg;

    /** 经度（Redis GEO 用） */
    private BigDecimal longitude;

    /** 纬度 */
    private BigDecimal latitude;

    private String openTime;

    /** 人均价格，单位：分 */
    private Integer avgPrice;

    private BigDecimal score;

    /** 累计预约数，冗余字段，用于排序 */
    private Integer soldCount;

    /** 推荐路线顺序，0 表示不进主路线 */
    private Integer recommendOrder;

    /** 建议游览时长（分钟） */
    private Integer walkMinutes;

    /** 1 景点 0 展馆 */
    private Integer type;

    /** 1 上架 0 下架 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
