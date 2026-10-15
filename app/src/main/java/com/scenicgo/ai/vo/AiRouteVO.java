package com.scenicgo.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 路线推荐结果。
 */
@Data
@AllArgsConstructor
public class AiRouteVO {

    /** 总时长（分钟）= 各点游览时长 + 相邻两点步行时长 */
    private Integer totalMinutes;

    private List<RouteSpotVO> spots;
}
