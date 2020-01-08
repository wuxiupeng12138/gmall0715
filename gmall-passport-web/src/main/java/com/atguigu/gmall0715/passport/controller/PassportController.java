package com.atguigu.gmall0715.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.UserInfo;
import com.atguigu.gmall0715.passport.config.JwtUtil;
import com.atguigu.gmall0715.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Value("${token.key}")
    private String key;

    @Reference
    private UserService userService;

    //获取用户点击的url后面的参数
    //https://passport.jd.com/new/login.aspx?ReturnUrl=https%3A%2%F%2Ewww.jd.com%2E
    //http://localhost:8087/index?originUrl=xxx
    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        //将登陆的地址获取到并保存到域中
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    //如何得到表单提交过来的数据
    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){
        //调用服务层
        UserInfo info = userService.login(userInfo);
        if(info != null){
            //data -- token
            //参数nickName
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId",info.getId());
            map.put("nickName",info.getNickName());
            //服务器的ip地址 配置nginx服务器代理 （从nginx配置但是从request请求头部中获取到）
            String salt = request.getHeader("X-forwarded-for");
            String token = JwtUtil.encode(key, map, salt);
            System.out.println(token);

            return token;
        }
        return "fail";
    }

    //直接将token、sale以参数的形式传入到控制器
    //http://passport.atguigu.com/verfiy?token=xxx&sale=xxx
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        //从token中获取userId ---> 解密token
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");
        Map<String, Object> map = JwtUtil.decode(token, key, salt);
        if(map != null && map.size() > 0){
            //从token中解密出来的 userId
            String userId = (String)map.get("userId");
            //调用服务层
            UserInfo userInfo = userService.verify(userId);
            if(userInfo != null){
                return "success";
            }
        }
        return "fail";
    }

}
