package com.scenicgo.auth.dto;

import lombok.Data;

/**
 * 登录参数。
 */
@Data
public class LoginFormDTO {

    private String username;

    private String password;
}
