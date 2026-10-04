package com.scenicgo.auth.interceptor;

import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.ResultCode;
import com.scenicgo.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 只负责拦：走到这里 {@link UserHolder} 里没有用户，就是没登录。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (UserHolder.get() == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未登录或登录已失效");
        }
        return true;
    }
}
