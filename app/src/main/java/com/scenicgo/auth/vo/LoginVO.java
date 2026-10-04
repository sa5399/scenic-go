package com.scenicgo.auth.vo;

import com.scenicgo.user.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录结果。
 */
@Data
@AllArgsConstructor
public class LoginVO {

    /** 后续请求放请求头 Authorization: Bearer {token} */
    private String token;

    private UserVO user;
}
