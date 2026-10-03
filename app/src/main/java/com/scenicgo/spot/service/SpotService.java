package com.scenicgo.spot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenicgo.spot.vo.SpotVO;

/**
 * 景点查询。
 */
public interface SpotService {

    /**
     * 分页查询上架景点。
     *
     * @param keyword 关键字，匹配名称或简介，可为空
     * @param type    1 景点 / 0 展馆，可为空表示不限
     * @param page    页码，从 1 开始
     * @param size    每页条数
     */
    Page<SpotVO> page(String keyword, Integer type, long page, long size);

    /**
     * 景点详情。
     *
     * @throws com.scenicgo.common.exception.BizException 景点不存在或已下架
     */
    SpotVO detail(Long id);
}
