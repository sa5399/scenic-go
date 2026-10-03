package com.scenicgo.config;

import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理：把异常收敛成统一的 {@link Result}，控制器里不再写 try-catch。
 *
 * <p>HTTP 状态码一律 200，失败原因放在 body 的 code/msg 里（见 {@link Result} 的约定）。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：可预期，记 warn 就够，不打堆栈 */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常 code={} msg={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 参数类异常：类型不匹配、缺必填参数 —— 属于客户端问题，不该报 500 */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public Result<Void> handleBadRequest(Exception e) {
        log.warn("参数异常: {}", e.getMessage());
        return Result.fail(Result.BAD_REQUEST, "参数不合法");
    }

    /** 兜底：非预期异常要打全堆栈，方便排查 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未处理异常", e);
        return Result.fail("服务器开小差了，请稍后再试");
    }
}
