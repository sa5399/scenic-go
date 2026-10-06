package com.scenicgo.reserve.service.impl;

import com.scenicgo.reserve.mapper.BookingSlotMapper;
import com.scenicgo.reserve.service.SlotService;
import com.scenicgo.reserve.service.StockService;
import com.scenicgo.reserve.vo.SlotVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 可预约时段查询。
 *
 * <p>列表和票价从库里取，余量以 Redis 为准：Redis 里没有该 key 才用 SQL 算出来的余量
 * （预热前的空窗期、或 Redis 被清空时都不会显示错）。
 */
@Service
@RequiredArgsConstructor
public class SlotServiceImpl implements SlotService {

    private final BookingSlotMapper bookingSlotMapper;

    private final StockService stockService;

    @Override
    public List<SlotVO> listSlots(Long spotId, LocalDate date) {
        List<SlotVO> slots = bookingSlotMapper.selectSlotList(spotId, date == null ? LocalDate.now() : date);
        if (slots.isEmpty()) {
            return slots;
        }
        Map<Long, Integer> stocks = stockService.readStocks(slots.stream().map(SlotVO::getSlotId).toList());
        slots.forEach(slot -> {
            Integer remaining = stocks.get(slot.getSlotId());
            if (remaining != null) {
                slot.setRemaining(remaining);
            }
        });
        return slots;
    }
}
