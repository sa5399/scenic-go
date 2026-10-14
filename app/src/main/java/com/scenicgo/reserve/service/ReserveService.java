package com.scenicgo.reserve.service;

import com.scenicgo.common.result.PageResult;
import com.scenicgo.reserve.dto.ReserveBrief;
import com.scenicgo.reserve.dto.ReserveFormDTO;
import com.scenicgo.reserve.dto.ReserveQuery;
import com.scenicgo.reserve.vo.ReserveCreateVO;
import com.scenicgo.reserve.vo.ReservePayVO;
import com.scenicgo.reserve.vo.ReserveVO;

public interface ReserveService {

    /** 下单：Lua 原子预扣 Redis 库存 → 建单 → DB 记已售 */
    ReserveCreateVO create(ReserveFormDTO form);

    /** 模拟支付：待支付 → 已预约，写入一次性核销码 */
    ReservePayVO pay(String orderNo);

    /** 取消：待支付 / 已预约 → 已取消，库存回补 */
    void cancel(String orderNo);

    /** 我的预约（分页 + 状态筛选），只返回当前登录用户的单 */
    PageResult<ReserveVO> listReserves(ReserveQuery query);

    /** 订单详情；不是自己的单抛 403 */
    ReserveVO getReserve(String orderNo);

    /**
     * 关掉待支付超时的单（已过期）并回补库存。
     *
     * @return 本次关掉的单数
     */
    int closeExpiredOrders();

    /**
     * 取自己的、已核销的单（跨模块用，比如发表评价时做校验）。
     *
     * <p>不存在 → 404；不是自己的 → 403；还没核销 → 500。状态语义留在 reserve 模块，
     * 别的模块不必依赖 {@code ReserveStatusEnum}。
     */
    ReserveBrief getMineCheckedIn(Long reserveId);
}
