package com.scenicgo.admin.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scenicgo.admin.vo.AdminReserveVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

public interface AdminReserveMapper {

    /** 运营订单查询：全部用户的单，带景点名与下单人昵称，按下单时间倒序 */
    IPage<AdminReserveVO> selectReservePage(IPage<AdminReserveVO> page,
                                            @Param("spotId") Long spotId,
                                            @Param("status") Integer status,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
}
