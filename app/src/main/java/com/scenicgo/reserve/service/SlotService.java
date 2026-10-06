package com.scenicgo.reserve.service;

import com.scenicgo.reserve.vo.SlotVO;

import java.time.LocalDate;
import java.util.List;

public interface SlotService {

    /** 某景点某天的可预约时段；date 传 null 按今天算 */
    List<SlotVO> listSlots(Long spotId, LocalDate date);
}
