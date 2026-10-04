package com.scenicgo.auth.service.impl;

import com.scenicgo.auth.LoginUser;
import com.scenicgo.auth.dto.LoginFormDTO;
import com.scenicgo.auth.dto.RegisterFormDTO;
import com.scenicgo.auth.service.AuthService;
import com.scenicgo.auth.vo.LoginVO;
import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.ResultCode;
import com.scenicgo.user.constant.UserStatusEnum;
import com.scenicgo.user.entity.User;
import com.scenicgo.user.service.UserService;
import com.scenicgo.user.vo.UserVO;
import com.scenicgo.utils.JsonUtils;
import com.scenicgo.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证实现。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final StringRedisTemplate stringRedisTemplate;

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

    @Override
    public LoginVO login(LoginFormDTO form) {
        User user = userService.getByUsername(form.getUsername());
        // 用户不存在与密码错误给同一个提示，避免暴露哪些用户名存在
        if (user == null || !passwordEncoder.matches(form.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (!UserStatusEnum.NORMAL.getCode().equals(user.getStatus())) {
            throw new BizException(ResultCode.FORBIDDEN, "账号已被封禁");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_TOKEN_KEY + token,
                JsonUtils.toJson(LoginUser.of(user)),
                RedisConstants.LOGIN_TOKEN_TTL_MINUTES, TimeUnit.MINUTES);
        return new LoginVO(token, UserVO.of(user));
    }

    @Override
    public void logout(String token) {
        stringRedisTemplate.delete(RedisConstants.LOGIN_TOKEN_KEY + token);
    }
}
