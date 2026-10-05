package com.scenicgo.spot.service;

import com.scenicgo.spot.dto.SpotDetail;

/**
 * 景点详情的缓存层。
 */
public interface SpotCacheService {

    /** 带缓存地取详情（景点 + 标签），返回 null 表示库里没有这个景点 */
    SpotDetail getSpotDetail(Long id);
}
