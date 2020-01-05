package com.atguigu.gmall0715.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class RedisConfig {
    //1.获取配置文件中的host、port、timeOut等参数

    //获取配置文件中的host，如果host没有数据，则给一个默认值: disabled
    @Value("${spring.redis.host:disabled}")
    private String host;
    @Value("${spring.redis.port:0}")
    private int port;
    @Value("${spring.redis.timeOut:10000}")
    private int timeOut;

    //2.将RedisUtil放入到spring容器中管理
    @Bean
    public RedisUtil getRedisUtils(){
        //如果没有host，则返回null
        if("disabled".equals(host)){
            return null;
        }
        RedisUtil redisUtil = new RedisUtil();
        //初始化连接池工厂
        redisUtil.initJedisPool(host, port, timeOut);
        return redisUtil;
    }
}
