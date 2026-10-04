package com.scenicgo.auth.service.impl;

import com.scenicgo.auth.dto.RegisterFormDTO;
import com.scenicgo.auth.service.AuthService;
import com.scenicgo.common.exception.BizException;
import com.scenicgo.user.entity.User;
import com.scenicgo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 认证实现。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterFormDTO form) {
        if (userService.getByUsername(form.getUsername()) != null) {
            throw new BizException("用户名已存在");
        }
        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setNickName(StringUtils.hasText(form.getNickName()) ? form.getNickName() : form.getUsername());
        user.setPhone(form.getPhone());
        // role / status 不设，走表里的默认值（0 游客 / 1 正常）
        userService.create(user);
    }
}
