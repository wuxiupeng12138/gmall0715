package com.atguigu.gmall0715.passport;

import com.atguigu.gmall0715.passport.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void testJWT(){
        //参数
        String key = "atguigu";
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId","1");
        map.put("nickName","admin");
        String salt = "47.94.91.51";
        String token = JwtUtil.encode(key, map, salt);
        System.out.println(token);

        System.out.println("---------------");

        Map<String, Object> map1 = JwtUtil.decode(token, key, salt);
        System.out.println(map1);
        Map<String, Object> map2 = JwtUtil.decode(token, "hello", salt);
        System.out.println(map2);
        Map<String, Object> map3 = JwtUtil.decode("eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6ImFkbWluIiwidXNlcklkIjoiMSJ9.n21mFX4jOuX2BSIS0pv3e5dOKxEmLI3BIhHxsvX6e8c", key, salt);
        System.out.println(map3);
        Map<String, Object> map4 = JwtUtil.decode(token, key, "192.168.113.226");
        System.out.println(map4);
    }
}
