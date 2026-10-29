package com.scenicgo.utils;

import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.ResultCode;
import com.scenicgo.user.constant.UserRoleEnum;

/**
 * 角色校验：管理端接口只允许指定角色调用。
 *
 * <p>当前登录用户由拦截器写进 {@link UserHolder}，这里只做判断，不查库。
 */
public final class RoleGuard {

    private RoleGuard() {
    }

    /** 至少具备其中一个角色，否则 403 */
    public static void require(UserRoleEnum... roles) {
        Integer current = UserHolder.get() == null ? null : UserHolder.get().getRole();
        for (UserRoleEnum role : roles) {
            if (role.getCode().equals(current)) {
                return;
            }
        }
        throw new BizException(ResultCode.FORBIDDEN, "没有操作权限");
    }
}
