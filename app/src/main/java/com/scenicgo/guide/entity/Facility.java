package com.scenicgo.guide.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 园区设施。
 */
@Data
@TableName("facility")
public class Facility {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 1 洗手间 2 餐饮 3 入口 4 停车 5 医疗 6 商店 */
    private Integer type;

    private BigDecimal longitude;

    private BigDecimal latitude;

    /** 所属景点，0 表示公共设施 */
    private Long spotId;

    /** 电子围栏半径（米） */
    private Integer fenceRadius;

    private String openTime;

    /** 1 可用 0 停用 */
    private Integer status;

    private LocalDateTime createTime;
}
