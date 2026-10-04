package com.scenicgo.guide.dto;

import lombok.Data;

/**
 * 附近设施查询条件。
 */
@Data
public class FacilityQuery {

    /** 当前经度，不传按 0 处理（查不到东西就返回空） */
    private double longitude;

    /** 当前纬度 */
    private double latitude;

    /** 搜索半径（米） */
    private int radius = 500;

    /** 1 洗手间 2 餐饮 3 入口 4 停车 5 医疗 6 商店，不传表示不限 */
    private Integer type;
}
