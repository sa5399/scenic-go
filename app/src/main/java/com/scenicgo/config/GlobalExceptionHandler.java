package com.scenicgo.config;

import com.scenicgo.common.exception.BizException;
import com.scenicgo.common.result.Result;
import com.scenicgo.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponse;
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

    /**
     * 请求体不是合法 JSON（比如少了引号）：算"参数不合法"。
     *
     * <p>它是 {@code HttpMessageConversionException}，既不实现 {@link ErrorResponse}，
     * 也不在上面几个类型里，不单独接住就会掉进兜底变成 500。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // Spring 自己的 HTTP 异常（路径不存在 404、方法不支持 405 等）实现的是 ErrorResponse 接口，
        // 并不是 ErrorResponseException 的子类，只能按接口判断，否则会一律变成 500
        if (e instanceof ErrorResponse errorResponse) {
            int code = errorResponse.getStatusCode().value();
            log.warn("请求不合法 code={} msg={}", code, e.getMessage());
            return code == ResultCode.NOT_FOUND.getCode()
                    ? Result.fail(ResultCode.NOT_FOUND)
                    : Result.fail(code, "请求不合法");
        }
        log.error("未处理异常", e);
        return Result.fail(ResultCode.FAIL);
    }
}
