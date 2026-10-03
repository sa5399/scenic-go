package com.scenicgo.spot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scenicgo.spot.entity.Tag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TagMapper extends BaseMapper<Tag> {

    /** 某个景点挂的标签，按标签本身的 sort 排序 */
    List<Tag> selectBySpotId(@Param("spotId") Long spotId);
}
