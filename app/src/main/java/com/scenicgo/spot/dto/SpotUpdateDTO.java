package com.scenicgo.spot.dto;

import lombok.Data;

/**
 * 管理端编辑景点基础信息（名称 / 简介 / 价格）。
 */
@Data
public class SpotUpdateDTO {

    /** 景点名称 */
    private String name;

    /** 一句话简介 */
    private String summary;

    /** 人均价格，单位：分（0 表示免费） */
    private Integer avgPrice;
}
