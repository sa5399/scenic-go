package com.scenicgo.guide.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 推荐路线结果。
 */
@Data
@AllArgsConstructor
public class RouteVO {

    /** 总时长（分钟）= 各点游览时长 + 相邻两点步行时长 */
    private Integer totalMinutes;

    private List<RouteSpotVO> spots;
}
