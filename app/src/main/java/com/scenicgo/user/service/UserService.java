package com.scenicgo.user.service;

import com.scenicgo.user.entity.User;

/**
 * 用户。
 */
public interface UserService {

    /** 按用户名查，登录和注册判重用 */
    User getByUsername(String username);

    User getById(Long id);

    void create(User user);
}
