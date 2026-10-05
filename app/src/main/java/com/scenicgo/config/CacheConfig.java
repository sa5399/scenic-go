package com.scenicgo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 缓存相关配置。
 */
@Configuration
public class CacheConfig {

    /** 逻辑过期后异步重建用，不能让请求线程去等 */
    @Bean
    public ThreadPoolTaskExecutor cacheRebuildExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("cache-rebuild-");
        executor.initialize();
        return executor;
    }
}
