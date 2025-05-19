package com.iot.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API响应注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiResponse {
    String value() default "";
    int code() default 200;
    String message() default "操作成功";
    Class<?> response() default Void.class;
} 