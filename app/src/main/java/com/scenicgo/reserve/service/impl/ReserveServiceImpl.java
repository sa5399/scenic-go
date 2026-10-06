package com.scenicgo.reserve.service.impl;

import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.ResultCode;
import com.scenicgo.reserve.constant.ReserveConstants;
import com.scenicgo.reserve.constant.ReserveStatusEnum;
import com.scenicgo.reserve.constant.SlotStatusEnum;
import com.scenicgo.reserve.dto.ReserveFormDTO;
import com.scenicgo.reserve.entity.BookingSlot;
import com.scenicgo.reserve.entity.Reserve;
import com.scenicgo.reserve.entity.TicketType;
import com.scenicgo.reserve.mapper.BookingSlotMapper;
import com.scenicgo.reserve.mapper.ReserveMapper;
import com.scenicgo.reserve.mapper.TicketTypeMapper;
import com.scenicgo.reserve.service.ReserveService;
import com.scenicgo.reserve.service.StockService;
import com.scenicgo.reserve.vo.ReserveCreateVO;
import com.scenicgo.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 下单实现。
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
        return ReserveConstants.ORDER_NO_PREFIX + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + randomSuffix();
    }

    /** 8 位随机数字；撞号由 uk_order_no / uk_checkin_code 兜底 */
    private String randomSuffix() {
        return "%08d".formatted(ThreadLocalRandom.current().nextInt(ReserveConstants.RANDOM_BOUND));
    }
}
