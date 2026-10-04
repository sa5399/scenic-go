package com.scenicgo.guide.service;

import com.scenicgo.guide.dto.FacilityQuery;
import com.scenicgo.guide.vo.FacilityVO;

import java.util.List;

/**
 * 园区设施。
 */
public interface FacilityService {

    /** 附近设施，按距离升序 */
    List<FacilityVO> listNearby(FacilityQuery query);

    /** 把设施坐标写进 Redis GEO */
    void refreshGeoCache();
}
