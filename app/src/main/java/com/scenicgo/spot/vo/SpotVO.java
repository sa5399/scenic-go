package com.scenicgo.spot.vo;

import com.scenicgo.spot.entity.Spot;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

/**
 * 景点对外视图 —— 不直接把 entity 丢给前端。
 *
 * <p>列表查询只填非 content 的字段（{@code content} 为 TEXT，列表不查它），详情才会带上 content。
 */
@Data
public class SpotVO {

    private Long id;
    private String name;
    private String summary;
    /** 详细介绍：列表接口为 null，详情接口才有 */
    private String content;
    private String coverImg;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String openTime;
    /** 人均价格，单位：分 */
    private Integer avgPrice;
    private BigDecimal score;
    private Integer soldCount;
    private Integer recommendOrder;
    private Integer walkMinutes;
    private Integer type;

    public static SpotVO of(Spot spot) {
        SpotVO vo = new SpotVO();
        BeanUtils.copyProperties(spot, vo);
        return vo;
    }
}
