package com.scenicgo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档，/swagger-ui.html。
 *
 * <p>声明 bearer 鉴权后，Swagger 右上角会出现 Authorize，填一次 token 就会自动带到请求头，
 * 方便点着调受保护接口（免登录接口带上 token 也无害）。
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI scenicGoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("游小景 · 景区分时预约与电子导览系统")
                        .description("分时预约 / 电子导览 / AI 场景推荐 —— 接口文档")
                        .version("v1"))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .description("登录接口返回的 token，直接粘贴（不用手写 Bearer 前缀）")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
