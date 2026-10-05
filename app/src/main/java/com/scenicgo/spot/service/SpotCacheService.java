package com.scenicgo.spot.service;

import com.scenicgo.spot.entity.Spot;

/**
 * 景点详情的缓存层。
 */
public interface SpotCacheService {

    /** 带缓存地查景点，返回 null 表示库里也没有 */
    Spot getSpot(Long id);
}
