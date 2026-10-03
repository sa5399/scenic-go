package com.scenicgo.common.exception;

import com.scenicgo.common.result.Result;

/**
 * 业务异常：可预期的失败（库存不足、景点已下架……）。
 *
 * <p>由全局异常处理器统一转成 {@link Result}，业务代码里不要自己 try-catch 后拼返回值。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(Result.FAIL, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
