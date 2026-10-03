package com.scenicgo.spot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenicgo.common.exception.BizException;
import com.scenicgo.spot.entity.Spot;
import com.scenicgo.spot.mapper.SpotMapper;
import com.scenicgo.spot.service.SpotService;
import com.scenicgo.spot.vo.SpotVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 景点查询实现。
 *
 * <p>暂时不加缓存：先把数据库链路跑通，Redis 缓存单独一次提交再加。
 */
@Service
@RequiredArgsConstructor
public class SpotServiceImpl implements SpotService {

    /** 上架状态 */
    private static final int STATUS_ON = 1;

    private final SpotMapper spotMapper;

    @Override
    public Page<SpotVO> page(String keyword, Integer type, long page, long size) {
        LambdaQueryWrapper<Spot> query = new LambdaQueryWrapper<Spot>()
                .eq(Spot::getStatus, STATUS_ON)
                .eq(type != null, Spot::getType, type)
                .and(StringUtils.hasText(keyword),
                        w -> w.like(Spot::getName, keyword).or().like(Spot::getSummary, keyword))
                .orderByDesc(Spot::getSoldCount)
                .orderByDesc(Spot::getScore)
                // 列表不查 content（TEXT），少传一大段文本
                .select(Spot::getId, Spot::getName, Spot::getSummary, Spot::getCoverImg,
                        Spot::getLongitude, Spot::getLatitude, Spot::getOpenTime,
                        Spot::getAvgPrice, Spot::getScore, Spot::getSoldCount,
                        Spot::getRecommendOrder, Spot::getWalkMinutes, Spot::getType);

        Page<Spot> result = spotMapper.selectPage(Page.of(page, size), query);

        Page<SpotVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(SpotVO::of).toList());
        return voPage;
    }

    @Override
    public SpotVO detail(Long id) {
        Spot spot = spotMapper.selectById(id);
        if (spot == null || !Integer.valueOf(STATUS_ON).equals(spot.getStatus())) {
            throw new BizException("景点不存在或已下架");
        }
        return SpotVO.of(spot);
    }
}
