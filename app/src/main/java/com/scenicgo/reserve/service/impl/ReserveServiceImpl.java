package com.scenicgo.reserve.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.PageResult;
import com.scenicgo.common.result.ResultCode;
import com.scenicgo.reserve.constant.ReserveConstants;
import com.scenicgo.reserve.constant.ReserveStatusEnum;
import com.scenicgo.reserve.constant.SlotStatusEnum;
import com.scenicgo.reserve.dto.ReserveBrief;
import com.scenicgo.reserve.dto.ReserveFormDTO;
import com.scenicgo.reserve.dto.ReserveQuery;
import com.scenicgo.reserve.entity.BookingSlot;
import com.scenicgo.reserve.entity.Reserve;
import com.scenicgo.reserve.entity.TicketType;
import com.scenicgo.reserve.mapper.BookingSlotMapper;
import com.scenicgo.reserve.mapper.ReserveMapper;
import com.scenicgo.reserve.mapper.TicketTypeMapper;
import com.scenicgo.reserve.service.ReserveService;
import com.scenicgo.reserve.service.StockService;
import com.scenicgo.reserve.vo.ReserveCreateVO;
import com.scenicgo.reserve.vo.ReservePayVO;
import com.scenicgo.reserve.vo.ReserveVO;
import com.scenicgo.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 预约单实现：下单预扣、支付、取消、超时关单。
 *
 * <p>状态流转一律用「带原状态条件的 UPDATE」而不是先查后改：支付与超时关单会抢同一张单，
 * 影响行数为 0 就说明别人先改了，按业务异常返回即可。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReserveServiceImpl implements ReserveService {

    /** 时段起止是定长 "HH:mm"，字符串比较等价于时间比较 */
    private static final DateTimeFormatter HOUR_MINUTE = DateTimeFormatter.ofPattern("HH:mm");

    private final BookingSlotMapper bookingSlotMapper;

    private final TicketTypeMapper ticketTypeMapper;

    private final ReserveMapper reserveMapper;

    private final StockService stockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReserveCreateVO create(ReserveFormDTO form) {
        BookingSlot slot = bookingSlotMapper.selectById(form.getSlotId());
        if (slot == null || !SlotStatusEnum.SALABLE.getCode().equals(slot.getStatus())) {
            throw new BizException(ResultCode.NOT_FOUND, "时段不存在或已停售");
        }
        TicketType ticketType = ticketTypeMapper.selectById(slot.getTicketType());
        if (form.getQuantity() > ticketType.getLimitPerSlot()) {
            throw new BizException("每单最多 " + ticketType.getLimitPerSlot() + " 张");
        }
        checkSlotOpen(slot);

        // 先扣 Redis 再落库：宁可少卖也不能超卖，建单失败会在 catch 里还回去
        stockService.deduct(slot.getId(), form.getQuantity());
        try {
            Reserve reserve = buildReserve(UserHolder.get().getId(), slot, form.getQuantity());
            reserveMapper.insert(reserve);
            if (bookingSlotMapper.increaseSoldStock(slot.getId(), form.getQuantity()) == 0) {
                throw new BizException("余票不足");
            }
            log.info("下单成功 orderNo={} userId={} slotId={} quantity={}",
                    reserve.getOrderNo(), reserve.getUserId(), slot.getId(), form.getQuantity());
            return new ReserveCreateVO(reserve.getOrderNo(), reserve.getTotalAmount(),
                    reserve.getStatus(), ReserveConstants.PAY_TIMEOUT_MINUTES);
        } catch (RuntimeException e) {
            // 库存脚本已经扣过，这里必须还回去，否则这些票永远卖不出去
            stockService.reback(slot.getId(), form.getQuantity());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReservePayVO pay(String orderNo) {
        Reserve reserve = getOwnReserve(orderNo);
        String checkinCode = generateCheckinCode();
        LocalDateTime payTime = now();

        int rows = reserveMapper.update(null, new LambdaUpdateWrapper<Reserve>()
                .eq(Reserve::getId, reserve.getId())
                .eq(Reserve::getStatus, ReserveStatusEnum.PENDING.getCode())
                .set(Reserve::getStatus, ReserveStatusEnum.BOOKED.getCode())
                .set(Reserve::getCheckinCode, checkinCode)
                .set(Reserve::getPayTime, payTime));
        if (rows == 0) {
            throw new BizException("当前状态不可支付");
        }
        log.info("支付成功 orderNo={} checkinCode={}", orderNo, checkinCode);
        return new ReservePayVO(orderNo, ReserveStatusEnum.BOOKED.getCode(), checkinCode, payTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(String orderNo) {
        Reserve reserve = getOwnReserve(orderNo);
        Integer status = reserve.getStatus();
        if (!ReserveStatusEnum.PENDING.getCode().equals(status)
                && !ReserveStatusEnum.BOOKED.getCode().equals(status)) {
            throw new BizException("当前状态不可取消");
        }

        int rows = reserveMapper.update(null, new LambdaUpdateWrapper<Reserve>()
                .eq(Reserve::getId, reserve.getId())
                .eq(Reserve::getStatus, status)
                .set(Reserve::getStatus, ReserveStatusEnum.CANCELED.getCode())
                .set(Reserve::getCancelTime, now()));
        if (rows == 0) {
            throw new BizException("当前状态不可取消");
        }
        releaseStock(reserve);
        log.info("取消成功 orderNo={} quantity={}", orderNo, reserve.getQuantity());
    }

    @Override
    public PageResult<ReserveVO> listReserves(ReserveQuery query) {
        IPage<ReserveVO> page = reserveMapper.selectReservePage(
                Page.of(query.getCurrent(), query.getSize()), UserHolder.get().getId(), query.getStatus());
        return PageResult.of(page);
    }

    @Override
    public ReserveVO getReserve(String orderNo) {
        getOwnReserve(orderNo);
        return reserveMapper.selectDetailByOrderNo(orderNo);
    }

    @Override
    public int closeExpiredOrders() {
        LocalDateTime deadline = now().minusMinutes(ReserveConstants.PAY_TIMEOUT_MINUTES);
        List<Reserve> expired = reserveMapper.selectExpiredOrders(deadline, ReserveConstants.TIMEOUT_BATCH_SIZE);

        int closed = 0;
        for (Reserve reserve : expired) {
            try {
                // 和「支付」抢同一张单：谁先改成功算谁的
                int rows = reserveMapper.update(null, new LambdaUpdateWrapper<Reserve>()
                        .eq(Reserve::getId, reserve.getId())
                        .eq(Reserve::getStatus, ReserveStatusEnum.PENDING.getCode())
                        .set(Reserve::getStatus, ReserveStatusEnum.EXPIRED.getCode()));
                if (rows == 0) {
                    continue;
                }
                releaseStock(reserve);
                log.info("超时关单 orderNo={} quantity={}", reserve.getOrderNo(), reserve.getQuantity());
                closed++;
            } catch (Exception e) {
                // 一张单出问题不带停整批，下一分钟还会再扫到它
                log.warn("超时关单失败 orderNo={}", reserve.getOrderNo(), e);
            }
        }
        return closed;
    }

    /** 取消与超时关单都要还库存：先减 DB 记账，减成功再回补 Redis */
    private void releaseStock(Reserve reserve) {
        if (bookingSlotMapper.decreaseSoldStock(reserve.getSlotId(), reserve.getQuantity()) == 0) {
            // 记账里已经没有这笔了，Redis 也不加：宁可少卖，不能凭空多卖
            log.warn("时段记账不足，跳过库存回补 slotId={} quantity={}",
                    reserve.getSlotId(), reserve.getQuantity());
            return;
        }
        stockService.reback(reserve.getSlotId(), reserve.getQuantity());
    }

    /** 时段整体结束后不再放票；当天还没结束的时段都能下单 */
    private void checkSlotOpen(BookingSlot slot) {
        LocalDate today = LocalDate.now();
        if (slot.getSlotDate().isBefore(today)) {
            throw new BizException("该时段已停止预约");
        }
        if (slot.getSlotDate().isEqual(today)
                && slot.getSlotEnd().compareTo(LocalTime.now().format(HOUR_MINUTE)) < 0) {
            throw new BizException("该时段已停止预约");
        }
    }

    /** 下单、支付、取消、关单都按订单号找单，顺带校验是不是自己的 */
    private Reserve getOwnReserve(String orderNo) {
        Reserve reserve = reserveMapper.selectOne(new LambdaQueryWrapper<Reserve>()
                .eq(Reserve::getOrderNo, orderNo)
                .select(Reserve::getId, Reserve::getOrderNo, Reserve::getUserId,
                        Reserve::getSlotId, Reserve::getQuantity, Reserve::getStatus));
        if (reserve == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!reserve.getUserId().equals(UserHolder.get().getId())) {
            throw new BizException(ResultCode.FORBIDDEN, "没有权限操作该订单");
        }
        return reserve;
    }

    @Override
    public ReserveBrief getMineCheckedIn(Long reserveId) {
        Reserve reserve = reserveMapper.selectOne(new LambdaQueryWrapper<Reserve>()
                .eq(Reserve::getId, reserveId)
                .select(Reserve::getId, Reserve::getSpotId, Reserve::getUserId, Reserve::getStatus));
        if (reserve == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!reserve.getUserId().equals(UserHolder.get().getId())) {
            throw new BizException(ResultCode.FORBIDDEN, "没有权限操作该订单");
        }
        if (!ReserveStatusEnum.CHECKED_IN.getCode().equals(reserve.getStatus())) {
            throw new BizException("订单还没核销，不能关联评价");
        }
        ReserveBrief brief = new ReserveBrief();
        BeanUtils.copyProperties(reserve, brief);
        return brief;
    }

    /** 时段信息是下单当时的快照，库存表以后改了也不影响已下单的这张单 */
    private Reserve buildReserve(Long userId, BookingSlot slot, int quantity) {
        Reserve reserve = new Reserve();
        reserve.setOrderNo(generateOrderNo());
        reserve.setUserId(userId);
        reserve.setSpotId(slot.getSpotId());
        reserve.setSlotId(slot.getId());
        reserve.setSlotDate(slot.getSlotDate());
        reserve.setSlotStart(slot.getSlotStart());
        reserve.setSlotEnd(slot.getSlotEnd());
        reserve.setTicketType(slot.getTicketType());
        reserve.setQuantity(quantity);
        reserve.setUnitPrice(slot.getPrice());
        reserve.setTotalAmount(slot.getPrice() * quantity);
        reserve.setStatus(ReserveStatusEnum.PENDING.getCode());
        return reserve;
    }

    private String generateOrderNo() {
        return ReserveConstants.ORDER_NO_PREFIX + today() + randomSuffix();
    }

    private String generateCheckinCode() {
        return ReserveConstants.CHECKIN_CODE_PREFIX + today() + randomSuffix();
    }

    private String today() {
        return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    /** 秒级时间：DB 存的是 DATETIME(0)，带纳秒返回给前端会和库里对不上 */
    private LocalDateTime now() {
        return LocalDateTime.now().withNano(0);
    }

    /** 8 位随机数字；撞号由 uk_order_no / uk_checkin_code 兜底 */
    private String randomSuffix() {
        return "%08d".formatted(ThreadLocalRandom.current().nextInt(ReserveConstants.RANDOM_BOUND));
    }
}
