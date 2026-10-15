package com.scenicgo.guide.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 候选点：一次 join 查出来的「景点 × 标签」行，一个景点可能多行。
 *
 * <p>聚合在 service 里做，SQL 保持简单。
 */
@Data
public class RouteCandidate {

    private Long id;

    private String name;

    private BigDecimal longitude;

    private BigDecimal latitude;

    /** 建议游览时长（分钟） */
    private Integer walkMinutes;

    /** 推荐路线顺序，0 表示不进主路线 */
    private Integer recommendOrder;

    /** 标签名，没标签时为 null */
    private String tagName;
}
