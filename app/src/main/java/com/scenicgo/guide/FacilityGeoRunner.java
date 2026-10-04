package com.scenicgo.guide;

import com.scenicgo.guide.service.FacilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时把设施坐标预热进 Redis GEO。设施数据几乎不变，启动写一次就够。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityGeoRunner implements ApplicationRunner {

    private final FacilityService facilityService;

    @Override
    public void run(ApplicationArguments args) {
        facilityService.refreshGeoCache();
        log.info("设施 GEO 缓存已预热");
    }
}
