package com.scenicgo.ai.service;

import com.scenicgo.ai.dto.AiRouteFormDTO;
import com.scenicgo.ai.vo.AiRouteVO;

public interface AiRouteService {

    /** 按标签 + 人群 + 时间预算排一条有序路线；规则版，不依赖大模型 */
    AiRouteVO planRoute(AiRouteFormDTO form);
}
