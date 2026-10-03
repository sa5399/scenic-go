package com.scenicgo.spot.service;

import com.scenicgo.common.result.PageResult;
import com.scenicgo.spot.dto.SpotQuery;
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
}
