package com.scenicgo.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户。
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 哈希，不是明文 */
    private String password;

    private String nickName;

    private String icon;

    private String phone;

    private String email;

    /** 0 游客 1 会员 2 核销员 3 管理员 */
    private Integer role;

    /** 1 正常 0 封禁 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
