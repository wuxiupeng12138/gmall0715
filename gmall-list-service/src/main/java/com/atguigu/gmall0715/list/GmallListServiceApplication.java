package com.atguigu.gmall0715.list;

import com.atguigu.gmall0715.list.serviceimpl.ListServiceImpl;
import com.atguigu.gmall0715.service.ListService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class GmallListServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallListServiceApplication.class, args);
    }
}
