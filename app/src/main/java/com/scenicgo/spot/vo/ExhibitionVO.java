package com.scenicgo.spot.vo;

import com.scenicgo.spot.entity.Exhibition;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * 展品对外视图，不带讲解词。
 */
@Data
public class ExhibitionVO {

    private Long id;
    private Long spotId;
    private String name;
    private String category;
    private String summary;
    private String coverImg;
    private Integer sort;

    public static ExhibitionVO of(Exhibition exhibition) {
        ExhibitionVO vo = new ExhibitionVO();
        BeanUtils.copyProperties(exhibition, vo);
        return vo;
    }
}
