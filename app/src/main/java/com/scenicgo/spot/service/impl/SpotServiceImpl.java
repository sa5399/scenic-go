package com.scenicgo.spot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.PageResult;
import com.scenicgo.common.result.ResultCode;
import com.scenicgo.spot.constant.SpotStatusEnum;
import com.scenicgo.spot.dto.SpotDetail;
import com.scenicgo.spot.dto.SpotQuery;
import com.scenicgo.spot.entity.Exhibition;
import com.scenicgo.spot.entity.Spot;
import com.scenicgo.spot.mapper.ExhibitionMapper;
import com.scenicgo.spot.mapper.SpotMapper;
import com.scenicgo.spot.service.SpotCacheService;
import com.scenicgo.spot.service.SpotService;
import com.scenicgo.spot.vo.ExhibitionVO;
import com.scenicgo.spot.vo.SpotVO;
import com.scenicgo.spot.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 景点查询实现。
 */
@Service
@RequiredArgsConstructor
public class SpotServiceImpl implements SpotService {

    private final SpotMapper spotMapper;

    private final ExhibitionMapper exhibitionMapper;

    private final SpotCacheService spotCacheService;

    @Override
    public PageResult<SpotVO> listSpots(SpotQuery query) {
        List<Long> tagIds = query.getTagIds();
        IPage<Spot> page = spotMapper.selectSpotPage(
                Page.of(query.getCurrent(), query.getSize()),
                query.getKeyword(),
                query.getType(),
                tagIds,
                tagIds == null ? 0 : tagIds.size());
        return PageResult.of(page.convert(SpotVO::of));
    }

    @Override
    public SpotVO getSpot(Long id) {
        SpotDetail detail = spotCacheService.getSpotDetail(id);
        Spot spot = detail == null ? null : detail.getSpot();
        if (spot == null || !SpotStatusEnum.ON_SALE.getCode().equals(spot.getStatus())) {
            throw new BizException(ResultCode.NOT_FOUND, "景点不存在或已下架");
        }
        SpotVO vo = SpotVO.of(spot);
        vo.setTags(detail.getTags().stream().map(TagVO::of).toList());
        return vo;
    }

    @Override
    public List<ExhibitionVO> listExhibitions(Long spotId) {
        List<Exhibition> exhibitions = exhibitionMapper.selectList(new LambdaQueryWrapper<Exhibition>()
                .eq(Exhibition::getSpotId, spotId)
                .eq(Exhibition::getStatus, SpotStatusEnum.ON_SALE.getCode())
                .orderByAsc(Exhibition::getSort)
                // narrative 是 TEXT，列表不带
                .select(Exhibition::getId, Exhibition::getSpotId, Exhibition::getName,
                        Exhibition::getCategory, Exhibition::getSummary,
                        Exhibition::getCoverImg, Exhibition::getSort));
        return exhibitions.stream().map(ExhibitionVO::of).toList();
    }
}
