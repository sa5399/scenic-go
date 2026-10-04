package com.scenicgo.guide.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scenicgo.guide.constant.FacilityStatusEnum;
import com.scenicgo.guide.dto.FacilityQuery;
import com.scenicgo.guide.entity.Facility;
import com.scenicgo.guide.mapper.FacilityMapper;
import com.scenicgo.guide.service.FacilityService;
import com.scenicgo.guide.vo.FacilityVO;
import com.scenicgo.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoSearchCommandArgs;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 园区设施实现。
 *
 * <p>坐标放 Redis GEO 查距离，设施信息仍然从库里取：GEO 只负责"哪些近"，
 * 类型筛选、上下架这些还是 SQL 说了算。
 */
@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    /** 一次 GEO 查询最多捞多少个候选点 */
    private static final int MAX_CANDIDATES = 50;

    private final FacilityMapper facilityMapper;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<FacilityVO> listNearby(FacilityQuery query) {
        GeoResults<GeoLocation<String>> results = searchGeo(query);
        if (results.getContent().isEmpty()) {
            // 兜底：Redis 被清空或没预热过，补一次再查
            refreshGeoCache();
            results = searchGeo(query);
        }

        // GEO 返回的顺序已经是按距离升序，用 LinkedHashMap 保住这个顺序
        Map<Long, Double> distanceById = new LinkedHashMap<>();
        for (GeoResult<GeoLocation<String>> result : results.getContent()) {
            distanceById.put(Long.valueOf(result.getContent().getName()), result.getDistance().getValue());
        }
        if (distanceById.isEmpty()) {
            return List.of();
        }

        List<Facility> facilities = facilityMapper.selectList(new LambdaQueryWrapper<Facility>()
                .in(Facility::getId, distanceById.keySet())
                .eq(Facility::getStatus, FacilityStatusEnum.ENABLED.getCode())
                .eq(query.getType() != null, Facility::getType, query.getType()));
        Map<Long, Facility> facilityById = facilities.stream()
                .collect(Collectors.toMap(Facility::getId, Function.identity()));

        return distanceById.entrySet().stream()
                .filter(entry -> facilityById.containsKey(entry.getKey()))
                .map(entry -> FacilityVO.of(facilityById.get(entry.getKey()), entry.getValue()))
                .toList();
    }

    @Override
    public void refreshGeoCache() {
        List<Facility> facilities = facilityMapper.selectList(new LambdaQueryWrapper<Facility>()
                .eq(Facility::getStatus, FacilityStatusEnum.ENABLED.getCode())
                .select(Facility::getId, Facility::getLongitude, Facility::getLatitude));
        if (facilities.isEmpty()) {
            return;
        }
        Map<String, Point> points = facilities.stream().collect(Collectors.toMap(
                facility -> String.valueOf(facility.getId()),
                facility -> new Point(facility.getLongitude().doubleValue(), facility.getLatitude().doubleValue())));
        stringRedisTemplate.opsForGeo().add(RedisConstants.GEO_FACILITY_KEY, points);
    }

    private GeoResults<GeoLocation<String>> searchGeo(FacilityQuery query) {
        GeoSearchCommandArgs args = GeoSearchCommandArgs.newGeoSearchArgs()
                .includeDistance()
                .sortAscending()
                .limit(MAX_CANDIDATES);
        return stringRedisTemplate.opsForGeo().search(
                RedisConstants.GEO_FACILITY_KEY,
                GeoReference.fromCoordinate(query.getLongitude(), query.getLatitude()),
                new Distance(query.getRadius(), DistanceUnit.METERS),
                args);
    }
}
