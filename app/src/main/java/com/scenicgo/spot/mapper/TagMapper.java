package com.scenicgo.spot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scenicgo.spot.entity.Tag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TagMapper extends BaseMapper<Tag> {

    /** 某个景点挂的标签，按标签本身的 sort 排序 */
    List<Tag> selectBySpotId(@Param("spotId") Long spotId);

    /** 挂标签（管理端改价格引起免费状态变化时同步，IGNORE 兜住重复插入） */
    @Insert("INSERT IGNORE INTO spot_tag (spot_id, tag_id) VALUES (#{spotId}, #{tagId})")
    int insertSpotTag(@Param("spotId") Long spotId, @Param("tagId") Long tagId);

    /** 摘标签（同上，收费时摘掉"免费"） */
    @Delete("DELETE FROM spot_tag WHERE spot_id = #{spotId} AND tag_id = #{tagId}")
    int deleteSpotTag(@Param("spotId") Long spotId, @Param("tagId") Long tagId);
}
