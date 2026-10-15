package com.scenicgo.guide.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 路线上的一个点。
 */
@Data
@AllArgsConstructor
public class RouteSpotVO {

    private Long id;

    private String name;

    /** 建议游览时长（分钟） */
    private Integer walkMinutes;

    /** 为什么排它（命中标签、上一站步行多久），由规则拼出来 */
    private String reason;
}
