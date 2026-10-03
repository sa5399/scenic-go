package com.scenicgo.spot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scenicgo.spot.entity.Tag;
import com.scenicgo.spot.mapper.TagMapper;
import com.scenicgo.spot.service.TagService;
import com.scenicgo.spot.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签字典实现。
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    @Override
    public List<TagVO> listTags(Integer type) {
        List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                .eq(type != null, Tag::getType, type)
                .orderByAsc(Tag::getSort)
                .select(Tag::getId, Tag::getName, Tag::getType, Tag::getSort));
        return tags.stream().map(TagVO::of).toList();
    }
}
