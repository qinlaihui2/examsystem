package com.atguigu.exam.exceptionhands;

import com.atguigu.exam.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler
 *
 * @author 李嘉宇
 * @date 2025/10/31 15:13
 * @version 1.0
 * @description 全局异常处理器
 */
@Slf4j
@RestControllerAdvice//全局异常都在这里处理
public class GlobalExceptionHandler {


    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e) {
        //打印日志
        e.printStackTrace();//错误的堆栈信息先打印
        log.error("代码出现异常，详细信息为{}",e.getMessage());
        return Result.error(e.getMessage());
    }
}
