package com.atguigu.gmall0715.config;

import jdk.nashorn.internal.ir.annotations.Reference;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
//声明作用在那(当前是作用在方法上)
@Target({ElementType.METHOD})
//注解的生命周期
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequire {
    //是否需要登录标识
    boolean autoRedirect() default true;
}
