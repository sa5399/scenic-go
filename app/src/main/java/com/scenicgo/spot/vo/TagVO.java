package com.scenicgo.spot.vo;

import com.scenicgo.spot.entity.Tag;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * 标签对外视图。
 */
@Data
public class TagVO {

    private Long id;
    private String name;
    private Integer type;
    private Integer sort;

    public static TagVO of(Tag tag) {
        TagVO vo = new TagVO();
        BeanUtils.copyProperties(tag, vo);
        return vo;
    }
}
