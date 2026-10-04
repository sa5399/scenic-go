package com.scenicgo.auth.service;

import com.scenicgo.auth.dto.RegisterFormDTO;

/**
 * 认证。
 */
public interface AuthService {

    /** 注册，用户名重复抛 BizException */
    void register(RegisterFormDTO form);
}
