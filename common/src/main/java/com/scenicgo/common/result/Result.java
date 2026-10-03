package com.scenicgo.common.result;

import lombok.Data;

/**
 * 统一响应体。
 *
 * <p>约定：HTTP 状态码恒为 200，业务结果看 {@code code}（200 成功，其余为失败）。
 * 这样前端只需要判断一种结构，不用同时处理 HTTP 状态和业务码两套分支。
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> {

    /** 成功 */
    public static final int OK = 200;
    /** 通用失败 */
    public static final int FAIL = 500;
    /** 参数不合法 */
    public static final int BAD_REQUEST = 400;

    private int code;
    private String msg;
    private T data;

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ok() {
        return new Result<>(OK, "success", null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(OK, "success", data);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(FAIL, msg, null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
