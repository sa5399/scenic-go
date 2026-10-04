package com.scenicgo.auth.controller;

import com.scenicgo.auth.dto.LoginFormDTO;
import com.scenicgo.auth.dto.RegisterFormDTO;
import com.scenicgo.auth.service.AuthService;
import com.scenicgo.auth.vo.LoginVO;
import com.scenicgo.common.result.Result;
import com.scenicgo.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 */
@Tag(name = "认证", description = "注册 / 登录 / 登出")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterFormDTO form) {
        authService.register(form);
        return Result.ok();
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginFormDTO form) {
        return Result.ok(authService.login(form));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = resolveToken(request);
        if (StringUtils.hasText(token)) {
            authService.logout(token);
        }
        return Result.ok();
    }

    private String resolveToken(HttpServletRequest request) {
        String value = request.getHeader("Authorization");
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.startsWith(BEARER_PREFIX) ? value.substring(BEARER_PREFIX.length()) : value;
    }
}
