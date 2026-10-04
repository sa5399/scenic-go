package com.scenicgo.auth.dto;

import lombok.Data;

/**
 * 注册参数。
 */
@Data
public class RegisterFormDTO {

    private String username;

    private String password;

    /** 不传就用用户名当昵称 */
    private String nickName;

    private String phone;
}
