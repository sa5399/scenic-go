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

    /**
     * 免登录名单。
     *
     * <p>登出也在这里：它只是删 token，没有 token 时什么都不用做，
     * 让"token 已失效还想登出"的客户端也能拿到成功（幂等），不然前端会卡在登不出去。
     */
    private static final String[] WHITE_LIST = {
            "/auth/register", "/auth/login", "/auth/logout",
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
