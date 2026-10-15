package com.scenicgo.ai.mapper;

import com.scenicgo.ai.dto.RouteCandidate;

import java.util.List;

public interface AiRouteMapper {

    /**
     * 候选点：可售景点 + 各自的标签名（一个景点一个标签一行）。
     *
     * <p>只读 join 了 spot / spot_tag / tag 三张表 —— 本项目里 reserve 模块的流水查询也 join 过
     * spot，跨模块**只读**取数走一条 SQL 比 N+1 调用别的模块服务划算；要写别人的表或聚合多模块才上 facade。
     */
    List<RouteCandidate> selectRouteCandidates();
}
