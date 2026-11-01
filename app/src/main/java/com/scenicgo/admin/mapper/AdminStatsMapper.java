package com.scenicgo.admin.mapper;

import com.scenicgo.admin.vo.AdminStatsVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface AdminStatsMapper {

    /** 概览统计：五个单表 count 一次查回，todayStart 为今日零点 */
    AdminStatsVO countStats(@Param("todayStart") LocalDateTime todayStart);
}
