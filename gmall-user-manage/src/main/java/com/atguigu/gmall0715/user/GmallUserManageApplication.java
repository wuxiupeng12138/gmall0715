package com.atguigu.gmall0715.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall0715.user.mapper")
@ComponentScan(basePackages = "com.atguigu.gmall0715.config")
public class GmallUserManageApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallUserManageApplication.class, args);
    }
}
