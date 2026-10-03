package com.scenicgo.config;

import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.Result;
import com.scenicgo.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理：异常统一转成 Result。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常 code={} msg={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数绑到 POJO 上以后，类型对不上抛的是 BindException，
     * 不是 MethodArgumentTypeMismatchException —— 两个都要接住，否则会掉进兜底变成 500。
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        log.warn("参数绑定失败: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public Result<Void> handleBadRequest(Exception e) {
        log.warn("参数异常: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未处理异常", e);
        return Result.fail(ResultCode.FAIL);
    }
}
