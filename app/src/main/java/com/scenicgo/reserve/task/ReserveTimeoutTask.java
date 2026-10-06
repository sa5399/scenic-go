package com.scenicgo.reserve.task;

import com.scenicgo.reserve.service.ReserveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 超时关单：每分钟扫一遍待支付且超过 15 分钟的单，置为已过期并回补库存。
 *
 * <p>只负责按点触发，关单逻辑在 {@link ReserveService#closeExpiredOrders()} 里。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReserveTimeoutTask {

    private final ReserveService reserveService;

    @Scheduled(cron = "0 * * * * ?")
    public void closeExpiredOrders() {
        int closed = reserveService.closeExpiredOrders();
        if (closed > 0) {
            log.info("超时关单 {} 张", closed);
        }
    }
}
