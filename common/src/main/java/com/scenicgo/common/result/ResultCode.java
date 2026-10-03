package com.scenicgo.common.result;

import lombok.Getter;

/**
 * 统一错误码。业务码以后按模块追加。
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "参数不合法"),
    NOT_FOUND(404, "资源不存在"),
    FAIL(500, "服务器开小差了，请稍后再试");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
