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
     * <p>登出**不**在这里：登出必须带有效 token，否则删不掉任何东西。
     * 如果放行，没带 token 的登出会返回成功却什么都没删（静默失败，极难排查）。
     * token 已过期时前端会收到 401，按约定清掉本地登录态即可。
     */
    private static final String[] WHITE_LIST = {
            "/auth/register", "/auth/login",
            "/spots", "/spots/**",
            "/tags", "/tags/**",
            "/facilities", "/facilities/**",
            "/guide", "/guide/**",
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
