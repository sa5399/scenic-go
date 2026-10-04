package com.scenicgo.auth;

import com.scenicgo.user.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

/**
 * 登录用户：存在 Redis 里、也放在 {@link com.scenicgo.utils.UserHolder} 里。
 *
 * <p>只放业务要用的字段，不含密码；有变化时重新登录即可刷新。
 */
@Data
@NoArgsConstructor
public class LoginUser {

    private Long id;
    private String username;
    private String nickName;
    private String icon;

    /** 0 游客 1 会员 2 核销员 3 管理员 */
    private Integer role;

    public static LoginUser of(User user) {
        LoginUser loginUser = new LoginUser();
        BeanUtils.copyProperties(user, loginUser);
        return loginUser;
    }
}
