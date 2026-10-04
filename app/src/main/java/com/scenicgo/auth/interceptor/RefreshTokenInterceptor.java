package com.scenicgo.auth.interceptor;

import com.scenicgo.auth.LoginUser;
import com.scenicgo.utils.JsonUtils;
import com.scenicgo.utils.RedisConstants;
import com.scenicgo.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 解析 token：能解析出就把当前用户放进 {@link UserHolder} 并续期，解析不出也不拦（拦是下一个拦截器的事）。
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = resolveToken(request);
        if (token == null) {
            return true;
        }
        String key = RedisConstants.LOGIN_TOKEN_KEY + token;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return true;
        }
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_TOKEN_TTL_MINUTES, TimeUnit.MINUTES);
        UserHolder.set(JsonUtils.fromJson(json, LoginUser.class));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 线程是复用的，不清理会把上一个请求的用户带给下一个
        UserHolder.remove();
    }

    private String resolveToken(HttpServletRequest request) {
        String value = request.getHeader("Authorization");
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.startsWith(BEARER_PREFIX) ? value.substring(BEARER_PREFIX.length()) : value;
    }
}
