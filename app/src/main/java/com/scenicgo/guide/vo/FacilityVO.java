package com.scenicgo.guide.vo;

import com.scenicgo.guide.entity.Facility;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

/**
 * 设施对外视图，比实体多一个距离。
 */
@Data
public class FacilityVO {

    private Long id;
    private String name;

    /** 1 洗手间 2 餐饮 3 入口 4 停车 5 医疗 6 商店 */
    private Integer type;

    private BigDecimal longitude;
    private BigDecimal latitude;

    /** 所属景点，0 表示公共设施 */
    private Long spotId;

    private String openTime;

    /** 距离，单位米 */
    private double distance;

    public static FacilityVO of(Facility facility, double distance) {
        FacilityVO vo = new FacilityVO();
        BeanUtils.copyProperties(facility, vo);
        vo.setDistance(distance);
        return vo;
    }
}
