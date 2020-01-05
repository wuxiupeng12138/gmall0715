package com.atguigu.gmall0715.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

    //1.先创建一个连接池的工厂
    private JedisPool jedisPool;
    //1.1 初始化连接池工厂
    public void initJedisPool(String host,int port,int timeOut){
        //初始化配置参数
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        //设置最大连接数
        jedisPoolConfig.setMaxTotal(200);
        //如果说达到最大连接数，课程使进程进行排队等待
        jedisPoolConfig.setBlockWhenExhausted(true);
        //设置等待时间
        jedisPoolConfig.setMaxWaitMillis(10*1000);
        //设置最少剩余数
        jedisPoolConfig.setMinIdle(10);
        //表示获取到连接的时候，自检一下当前连接是否可以使用!
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(jedisPoolConfig,host,port,timeOut);
    }
    //2.从工厂中获取连接Jedis
    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
