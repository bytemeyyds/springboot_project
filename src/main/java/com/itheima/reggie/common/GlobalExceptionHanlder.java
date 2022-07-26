package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */

//拦截含有这些注解的类
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHanlder {

    /**
     * 对SQLIntegrityConstraintViolationException这种异常信息进行处理
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class) //处理这种异常
    public R<String> exceptionHanlder(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        if (ex.getMessage().contains("Duplicate entry")){
            String[] s = ex.getMessage().split(" ");
            String msg = s[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误");

    }

    /**
     * 对CustomException这种异常信息进行处理
     * @return
     */
    @ExceptionHandler(CustomException.class) //处理这种异常
    public R<String> exceptionHanlder(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());

    }
}
