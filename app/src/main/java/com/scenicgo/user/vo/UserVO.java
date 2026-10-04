package com.scenicgo.user.vo;

import com.scenicgo.user.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * 用户对外视图，不含密码。
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickName;
    private String icon;

    /** 0 游客 1 会员 2 核销员 3 管理员 */
    private Integer role;

    public static UserVO of(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
