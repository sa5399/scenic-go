package com.scenicgo.common.result;

import lombok.Data;

/**
 * 统一响应体。HTTP 状态码恒为 200，成败看 code。
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ok() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /** 通用失败码 + 自定义提示 */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(ResultCode.FAIL.getCode(), msg, null);
    }

    /** 直接给错误码，用枚举自带的提示 */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
