package com.scenicgo.reserve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scenicgo.reserve.entity.BookingSlot;
import com.scenicgo.reserve.vo.SlotVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingSlotMapper extends BaseMapper<BookingSlot> {

    /** 某景点某天的可售时段，带票种名称与限购张数 */
    List<SlotVO> selectSlotList(@Param("spotId") Long spotId, @Param("slotDate") LocalDate slotDate);

    /** 预热用：今天及以后的可售时段及其库存，只取算余量要用的列 */
    List<BookingSlot> selectPreheatSlots(@Param("today") LocalDate today);

    /** 记账：已售 +quantity；会超过总库存时不更新（返回 0），与表上的 CHECK 形成双保险 */
    int increaseSoldStock(@Param("slotId") Long slotId, @Param("quantity") int quantity);

    /** 记账：已售 -quantity；扣成负数时不更新（返回 0） */
    int decreaseSoldStock(@Param("slotId") Long slotId, @Param("quantity") int quantity);
}
