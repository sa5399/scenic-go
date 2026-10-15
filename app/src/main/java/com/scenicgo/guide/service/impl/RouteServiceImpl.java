package com.scenicgo.guide.service.impl;

import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.ResultCode;
import com.scenicgo.guide.constant.CrowdEnum;
import com.scenicgo.guide.dto.RouteCandidate;
import com.scenicgo.guide.dto.RouteFormDTO;
import com.scenicgo.guide.mapper.RouteMapper;
import com.scenicgo.guide.service.RouteService;
import com.scenicgo.guide.vo.RouteSpotVO;
import com.scenicgo.guide.vo.RouteVO;
import com.scenicgo.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 路线推荐，四条规则都写在代码里、可查：
 *
 * <ol>
 *   <li><b>tags 是硬筛选</b>：只留命中其中任意一个标签的点（像 /spots 的标签筛选那样，要求"全部命中"
 *       在路线场景太苛刻，日落+拍照+情侣同时满足往往一个都不剩）</li>
 *   <li><b>crowd 只加权不筛选</b>：带娃优先亲子点，但预算没用完时允许补其他点，不至于只出一个点</li>
 *   <li><b>排序</b>：命中标签数降序 → 推荐顺序升序；{@code recommend_order = 0}（不进主路线，多为展馆）
 *       排在最后，只有预算有余或用户明确点了它的标签时才入选</li>
 *   <li><b>预算</b>：按「该点游览时长 + 上一站走过来的时长」累加，放不下就跳过（后面可能有更短的）</li>
 * </ol>
 *
 * <p>{@code reason} 是把命中的标签与步行时长拼出来的，不编造理由。
 */
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    /** 步行速度：80 米/分钟（约 4.8 km/h），只用来估园内步行时间 */
    private static final int WALK_METERS_PER_MINUTE = 80;

    /** recommend_order = 0 的排序权重：不主动排进主路线，但排在所有正常顺序之后 */
    private static final int ORDER_LAST = Integer.MAX_VALUE;

    private final RouteMapper routeMapper;

    @Override
    public RouteVO planRoute(RouteFormDTO form) {
        if (form.getMinutes() <= 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "时间预算要大于 0 分钟");
        }

        Set<String> filterTags = new HashSet<>();
        if (form.getTags() != null) {
            filterTags.addAll(form.getTags());
        }
        Set<String> weightTags = new HashSet<>(filterTags);
        CrowdEnum crowd = CrowdEnum.of(form.getCrowd());
        if (crowd != null) {
            weightTags.add(crowd.getTagName());
        }

        return pickByBudget(rank(filterTags, weightTags), form);
    }

    /** 一次查全候选点（景点 × 标签多行），在内存里聚合成「景点 + 它的标签」 */
    private List<ScoredSpot> rank(Set<String> filterTags, Set<String> weightTags) {
        Map<Long, RouteCandidate> spotById = new LinkedHashMap<>();
        Map<Long, List<String>> tagsById = new HashMap<>();
        for (RouteCandidate row : routeMapper.selectRouteCandidates()) {
            spotById.putIfAbsent(row.getId(), row);
            if (row.getTagName() != null) {
                tagsById.computeIfAbsent(row.getId(), id -> new ArrayList<>()).add(row.getTagName());
            }
        }

        return spotById.values().stream()
                .filter(spot -> matchesFilter(tagsById.get(spot.getId()), filterTags))
                .map(spot -> new ScoredSpot(spot, hitTags(tagsById.get(spot.getId()), weightTags)))
                .sorted(Comparator.comparingInt((ScoredSpot scored) -> -scored.hitTags().size())
                        .thenComparingInt(scored -> orderKey(scored.spot())))
                .toList();
    }

    private boolean matchesFilter(List<String> tagNames, Set<String> filterTags) {
        return filterTags.isEmpty()
                || (tagNames != null && tagNames.stream().anyMatch(filterTags::contains));
    }

    private List<String> hitTags(List<String> tagNames, Set<String> weightTags) {
        if (tagNames == null || weightTags.isEmpty()) {
            return List.of();
        }
        return tagNames.stream().filter(weightTags::contains).toList();
    }

    private int orderKey(RouteCandidate spot) {
        Integer order = spot.getRecommendOrder();
        return order == null || order == 0 ? ORDER_LAST : order;
    }

    private RouteVO pickByBudget(List<ScoredSpot> ranked, RouteFormDTO form) {
        List<RouteSpotVO> picked = new ArrayList<>();
        int totalMinutes = 0;
        // 起点：没传就从第一个点开始算，第一段不计步行时间
        Double currentLng = originValue(form.getLongitude());
        Double currentLat = originValue(form.getLatitude());

        for (ScoredSpot scored : ranked) {
            RouteCandidate spot = scored.spot();
            int legMinutes = legMinutes(currentLng, currentLat, spot);
            int cost = legMinutes + spot.getWalkMinutes();
            if (totalMinutes + cost > form.getMinutes()) {
                continue;
            }
            picked.add(new RouteSpotVO(spot.getId(), spot.getName(), spot.getWalkMinutes(),
                    buildReason(scored, legMinutes)));
            totalMinutes += cost;
            currentLng = spot.getLongitude().doubleValue();
            currentLat = spot.getLatitude().doubleValue();
        }
        return new RouteVO(totalMinutes, picked);
    }

    private int legMinutes(Double fromLng, Double fromLat, RouteCandidate spot) {
        if (fromLng == null || fromLat == null) {
            return 0;
        }
        double meters = GeoUtils.distanceMeters(fromLng, fromLat,
                spot.getLongitude().doubleValue(), spot.getLatitude().doubleValue());
        return (int) Math.ceil(meters / WALK_METERS_PER_MINUTE);
    }

    /** 0 当没传：传 (0,0) 会被算成几千公里外，不如按"不知道起点"处理 */
    private Double originValue(Double value) {
        return value == null || value == 0D ? null : value;
    }

    private String buildReason(ScoredSpot scored, int legMinutes) {
        List<String> parts = new ArrayList<>();
        if (!scored.hitTags().isEmpty()) {
            parts.add("命中标签：" + String.join("、", scored.hitTags()));
        }
        if (legMinutes > 0) {
            parts.add("上一站步行约 " + legMinutes + " 分钟");
        }
        return parts.isEmpty() ? "按推荐顺序安排" : String.join("；", parts);
    }

    /** 聚合后的候选：一个景点 + 它命中的标签 */
    private record ScoredSpot(RouteCandidate spot, List<String> hitTags) {
    }
}
