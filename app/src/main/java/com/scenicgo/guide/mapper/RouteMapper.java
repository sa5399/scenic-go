package com.scenicgo.guide.mapper;

import com.scenicgo.guide.dto.RouteCandidate;

import java.util.List;

public interface RouteMapper {

    /**
     * 候选点：可售景点 + 各自的标签名（一个景点一个标签一行）。
     *
     * <p>一条 SQL 直接读 spot / spot_tag / tag：景点标签本来就属于导览要展示的数据，
     * 比 N+1 去调 spot 模块的服务划算；只有要写别人的表或聚合多个模块时才需要 facade。
     */
    List<RouteCandidate> selectRouteCandidates();
}
