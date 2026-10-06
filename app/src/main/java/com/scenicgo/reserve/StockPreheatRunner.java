package com.scenicgo.reserve;

import com.scenicgo.reserve.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时把今天及以后的时段库存预热进 Redis。
 *
 * <p>只做一次：新一天的库存在造数脚本里生成，跑完重启即可（见开发文档 7.2）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockPreheatRunner implements ApplicationRunner {

    private final StockService stockService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("时段库存已预热 {} 个", stockService.preheat());
    }
}
