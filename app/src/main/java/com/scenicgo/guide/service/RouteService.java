package com.scenicgo.guide.service;

import com.scenicgo.guide.dto.RouteFormDTO;
import com.scenicgo.guide.vo.RouteVO;

public interface RouteService {

    /** 按标签 + 人群 + 时间预算排一条有序路线 */
    RouteVO planRoute(RouteFormDTO form);
}
