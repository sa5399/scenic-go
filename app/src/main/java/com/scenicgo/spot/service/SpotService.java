package com.scenicgo.spot.service;

import com.scenicgo.common.result.PageResult;
import com.scenicgo.spot.dto.SpotQuery;
import com.scenicgo.spot.dto.SpotUpdateDTO;
import com.scenicgo.spot.vo.ExhibitionVO;
import com.scenicgo.spot.vo.SpotVO;

import java.util.List;

/**
 * 景点查询。
 */
public interface SpotService {

    /** 分页查询上架景点 */
    PageResult<SpotVO> listSpots(SpotQuery query);

    /** 景点详情，不存在或已下架抛 BizException */
    SpotVO getSpot(Long id);

    /** 景点下的展品，按馆内排序 */
    List<ExhibitionVO> listExhibitions(Long spotId);

    /** 上下架（管理端）：1 上架 0 下架；改库后删详情缓存 */
    void updateStatus(Long id, Integer status);

    /** 管理端列表（含下架）：status 不传表示不限，按销量与评分排序 */
    PageResult<SpotVO> listForAdmin(String keyword, Integer type, Integer status, int current, int size);

    /** 管理端编辑基础信息（名称 / 简介 / 价格），改库后删详情缓存 */
    void updateForAdmin(Long id, SpotUpdateDTO dto);
}
