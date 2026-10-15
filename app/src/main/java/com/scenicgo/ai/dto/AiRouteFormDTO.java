package com.scenicgo.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * 路线推荐入参。
 */
@Data
public class AiRouteFormDTO {

    /** 想要的标签名，如 ["室内", "日落"]；不传表示不限 */
    private List<String> tags;

    /** 人群：family / couple / student，会折算成对应的人群标签；认不出来按不指定处理 */
    private String crowd;

    /** 时间预算（分钟），必须 > 0 */
    private int minutes;

    /** 起点经纬度，不传就从第一个点开始算（第一段不计步行时间） */
    private Double longitude;

    private Double latitude;
}
