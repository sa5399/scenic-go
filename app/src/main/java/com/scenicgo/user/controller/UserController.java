package com.scenicgo.user.controller;

import com.scenicgo.common.result.Result;
import com.scenicgo.user.service.UserService;
import com.scenicgo.user.vo.UserVO;
import com.scenicgo.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口。
 */
@Tag(name = "用户", description = "用户信息")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "当前登录用户")
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(UserVO.of(userService.getById(UserHolder.get().getId())));
    }
}
