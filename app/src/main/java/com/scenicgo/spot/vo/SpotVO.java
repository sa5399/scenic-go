package com.scenicgo.spot.vo;

import com.scenicgo.spot.entity.Spot;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 景点对外视图：列表不带 content 与 tags，详情才带。
 */
@Data
public class SpotVO {

    private Long id;
    private String name;
    private String summary;
    private String content;
    private String coverImg;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String openTime;

    /** 单位：分 */
    private Integer avgPrice;

    private BigDecimal score;
    private Integer soldCount;
    private Integer recommendOrder;
    private Integer walkMinutes;

    /** 1 景点 0 展馆 */
    private Integer type;

    /** 1 上架 0 下架（管理端列表用） */
    private Integer status;

    /** 景点挂的标签，只有详情接口会填 */
    private List<TagVO> tags;

    public static SpotVO of(Spot spot) {
        SpotVO vo = new SpotVO();
        BeanUtils.copyProperties(spot, vo);
        return vo;
    }
}
