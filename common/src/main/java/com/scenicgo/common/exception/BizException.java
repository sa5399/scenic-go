package com.scenicgo.common.exception;

import com.scenicgo.common.result.ResultCode;

/**
 * 业务异常：可预期的失败。由全局异常处理器转成 Result。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(ResultCode.FAIL.getCode(), message);
    }

    public BizException(ResultCode resultCode, String message) {
        this(resultCode.getCode(), message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
