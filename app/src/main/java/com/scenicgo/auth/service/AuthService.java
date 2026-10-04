package com.scenicgo.auth.service;

import com.scenicgo.auth.dto.LoginFormDTO;
import com.scenicgo.auth.dto.RegisterFormDTO;
import com.scenicgo.auth.vo.LoginVO;

/**
 * 认证。
 */
public interface AuthService {

    /** 注册，用户名重复抛 BizException */
    void register(RegisterFormDTO form);

    /** 登录：校验密码、发 token、把登录用户写进 Redis */
    LoginVO login(LoginFormDTO form);

    /** 登出：删掉 Redis 里的 token */
    void logout(String token);
}
