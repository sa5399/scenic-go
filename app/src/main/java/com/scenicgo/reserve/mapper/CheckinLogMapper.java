package com.scenicgo.reserve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scenicgo.reserve.entity.CheckinLog;
import com.scenicgo.reserve.vo.CheckinLogVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

public interface CheckinLogMapper extends BaseMapper<CheckinLog> {

    /**
     * 核销流水分页。
     *
     * @param endDateExclusive 结束日期的次日零点：用 {@code < } 比较才走得上 checkin_time 的索引
     */
    IPage<CheckinLogVO> selectLogPage(IPage<CheckinLogVO> page,
                                      @Param("spotId") Long spotId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDateExclusive") LocalDate endDateExclusive);
}
