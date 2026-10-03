package com.scenicgo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档，/swagger-ui.html。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI scenicGoOpenApi() {
        return new OpenAPI().info(new Info()
                .title("游小景 · 景区分时预约与电子导览系统")
                .description("分时预约 / 电子导览 / AI 场景推荐 —— 接口文档")
                .version("v1"));
    }
}
