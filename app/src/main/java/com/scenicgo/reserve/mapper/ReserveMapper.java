package com.scenicgo.reserve.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scenicgo.reserve.entity.Reserve;
import com.scenicgo.reserve.vo.ReserveVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReserveMapper extends BaseMapper<Reserve> {

    /** 我的预约分页，带景点名 */
    IPage<ReserveVO> selectReservePage(IPage<ReserveVO> page,
                                       @Param("userId") Long userId,
                                       @Param("status") Integer status);

    /** 订单详情，带景点名 */
    ReserveVO selectDetailByOrderNo(@Param("orderNo") String orderNo);

    /** 待支付且已超时的单，只取关单回补要用的列 */
    List<Reserve> selectExpiredOrders(@Param("deadline") LocalDateTime deadline, @Param("limit") int limit);
}
