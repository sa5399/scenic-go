package com.scenicgo.spot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scenicgo.spot.entity.Spot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 启动类的 @MapperScan 已覆盖本包，接口上不用加 @Mapper。
 */
public interface SpotMapper extends BaseMapper<Spot> {

    /**
     * 景点列表：关键字、类型、标签三个条件都可选。
     *
     * <p>标签是「全部命中」语义（同时带上这些标签的景点才返回），
     * tagCount 就是 tagIds 的个数，由调用方传进来供 HAVING 比较。
     */
    IPage<Spot> selectSpotPage(IPage<Spot> page,
                               @Param("keyword") String keyword,
                               @Param("type") Integer type,
                               @Param("tagIds") List<Long> tagIds,
                               @Param("tagCount") int tagCount);
}
