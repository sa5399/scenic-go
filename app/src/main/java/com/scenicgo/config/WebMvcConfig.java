package com.scenicgo.config;

import com.scenicgo.auth.interceptor.LoginInterceptor;
import com.scenicgo.auth.interceptor.RefreshTokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器注册：读接口免登录，其余要带 token。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /** 免登录：注册登录、景点/标签/设施的读接口、接口文档 */
    private static final String[] WHITE_LIST = {
            "/auth/register", "/auth/login",
            "/spots", "/spots/**",
            "/tags", "/tags/**",
            "/facilities", "/facilities/**",
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            "/error"
    };

    private final RefreshTokenInterceptor refreshTokenInterceptor;

    private final LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(refreshTokenInterceptor).addPathPatterns("/**").order(0);
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(WHITE_LIST)
                .order(1);
    }
}
