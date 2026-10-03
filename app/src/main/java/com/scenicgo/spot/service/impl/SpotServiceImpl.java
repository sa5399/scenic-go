package com.scenicgo.spot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.PageResult;
import com.scenicgo.spot.constant.SpotStatusEnum;
import com.scenicgo.spot.dto.SpotQuery;
import com.scenicgo.spot.entity.Spot;
import com.scenicgo.spot.mapper.SpotMapper;
import com.scenicgo.spot.service.SpotService;
import com.scenicgo.spot.vo.SpotVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 景点查询实现。
 */
@Service
@RequiredArgsConstructor
public class SpotServiceImpl implements SpotService {

    private final SpotMapper spotMapper;

    @Override
    public PageResult<SpotVO> listSpots(SpotQuery query) {
        LambdaQueryWrapper<Spot> wrapper = new LambdaQueryWrapper<Spot>()
                .eq(Spot::getStatus, SpotStatusEnum.ON_SALE.getCode())
                .eq(query.getType() != null, Spot::getType, query.getType())
                .and(StringUtils.hasText(query.getKeyword()),
                        w -> w.like(Spot::getName, query.getKeyword())
                                .or().like(Spot::getSummary, query.getKeyword()))
                .orderByDesc(Spot::getSoldCount)
                .orderByDesc(Spot::getScore)
                // content 是 TEXT，列表不带
                .select(Spot::getId, Spot::getName, Spot::getSummary, Spot::getCoverImg,
                        Spot::getLongitude, Spot::getLatitude, Spot::getOpenTime,
                        Spot::getAvgPrice, Spot::getScore, Spot::getSoldCount,
                        Spot::getRecommendOrder, Spot::getWalkMinutes, Spot::getType);

        Page<Spot> result = spotMapper.selectPage(Page.of(query.getCurrent(), query.getSize()), wrapper);
        return PageResult.of(result.convert(SpotVO::of));
    }

    @Override
    public SpotVO getSpot(Long id) {
        Spot spot = spotMapper.selectById(id);
        if (spot == null || !SpotStatusEnum.ON_SALE.getCode().equals(spot.getStatus())) {
            throw new BizException("景点不存在或已下架");
        }
        return SpotVO.of(spot);
    }
}
