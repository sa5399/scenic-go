package com.scenicgo.spot.service;

import com.scenicgo.spot.vo.TagVO;

import java.util.List;

/**
 * 标签字典。
 */
public interface TagService {

    /** 标签列表，type 为空表示不限 */
    List<TagVO> listTags(Integer type);
}
