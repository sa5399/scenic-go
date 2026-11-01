package com.scenicgo.spot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.PageResult;
import com.scenicgo.common.result.ResultCode;
import com.scenicgo.spot.constant.SpotStatusEnum;
import com.scenicgo.spot.dto.SpotDetail;
import com.scenicgo.spot.dto.SpotQuery;
import com.scenicgo.spot.dto.SpotUpdateDTO;
import com.scenicgo.spot.entity.Exhibition;
import com.scenicgo.spot.entity.Spot;
import com.scenicgo.spot.entity.Tag;
import com.scenicgo.spot.mapper.ExhibitionMapper;
import com.scenicgo.spot.mapper.SpotMapper;
import com.scenicgo.spot.mapper.TagMapper;
import com.scenicgo.spot.service.SpotCacheService;
import com.scenicgo.spot.service.SpotService;
import com.scenicgo.spot.vo.ExhibitionVO;
import com.scenicgo.spot.vo.SpotVO;
import com.scenicgo.spot.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 景点查询实现。
 */
@Service
@RequiredArgsConstructor
public class SpotServiceImpl implements SpotService {

    private final SpotMapper spotMapper;

    private final ExhibitionMapper exhibitionMapper;

    private final TagMapper tagMapper;

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
    public PageResult<SpotVO> listForAdmin(String keyword, Integer type, Integer status, int current, int size) {
        LambdaQueryWrapper<Spot> wrapper = new LambdaQueryWrapper<Spot>()
                .select(Spot::getId, Spot::getName, Spot::getSummary, Spot::getCoverImg,
                        Spot::getLongitude, Spot::getLatitude, Spot::getOpenTime, Spot::getAvgPrice,
                        Spot::getScore, Spot::getSoldCount, Spot::getRecommendOrder,
                        Spot::getWalkMinutes, Spot::getType, Spot::getStatus)
                .eq(type != null, Spot::getType, type)
                .eq(status != null, Spot::getStatus, status)
                .orderByDesc(Spot::getSoldCount)
                .orderByDesc(Spot::getScore)
                .orderByDesc(Spot::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Spot::getName, keyword).or().like(Spot::getSummary, keyword));
        }
        IPage<Spot> page = spotMapper.selectPage(Page.of(current, size), wrapper);
        return PageResult.of(page.convert(SpotVO::of));
    }

    @Override
    public void updateForAdmin(Long id, SpotUpdateDTO dto) {
        if (spotMapper.selectById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "景点不存在");
        }
        if (dto.getName() == null || dto.getName().isBlank()
                || dto.getSummary() == null || dto.getSummary().isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "名称和简介不能为空");
        }
        int avgPrice = dto.getAvgPrice() == null ? 0 : dto.getAvgPrice();
        if (avgPrice < 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "价格不能为负数");
        }
        spotMapper.update(null, new LambdaUpdateWrapper<Spot>()
                .eq(Spot::getId, id)
                .set(Spot::getName, dto.getName().trim())
                .set(Spot::getSummary, dto.getSummary().trim())
                .set(Spot::getAvgPrice, avgPrice));
        // 免费 / 收费状态变化时同步"免费"标签，保证标签筛选与价格一致
        syncFreeTag(id, avgPrice == 0);
        // Cache-Aside：先改库再删详情缓存，游客端下次读自动重建
        spotCacheService.evict(id);
    }

    /** 免费（价格 0）与"免费"标签永远成对出现，避免标签筛选和实际价格脱节 */
    private void syncFreeTag(Long spotId, boolean free) {
        Tag freeTag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, "免费"));
        if (freeTag == null) {
            return;
        }
        if (free) {
            tagMapper.insertSpotTag(spotId, freeTag.getId());
        } else {
            tagMapper.deleteSpotTag(spotId, freeTag.getId());
        }
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

    @Override
    public void updateStatus(Long id, Integer status) {
        if (!SpotStatusEnum.ON_SALE.getCode().equals(status) && !SpotStatusEnum.OFF_SALE.getCode().equals(status)) {
            throw new BizException(ResultCode.BAD_REQUEST, "状态只能是 0 或 1");
        }
        if (spotMapper.selectById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "景点不存在");
        }
        spotMapper.update(null, new LambdaUpdateWrapper<Spot>()
                .eq(Spot::getId, id)
                .set(Spot::getStatus, status));
        // Cache-Aside：先改库再删缓存，下次读自动重建
        spotCacheService.evict(id);
    }
}
