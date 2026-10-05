package com.scenicgo.spot.dto;

import com.scenicgo.spot.entity.Spot;
import com.scenicgo.spot.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 景点详情缓存的对象：详情页要用的数据一起缓存，命中时一次库都不用查。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotDetail {

    private Spot spot;

    private List<Tag> tags;
}
