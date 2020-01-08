package com.atguigu.gmall0715.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    //进入控制器之前执行
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //当我们在登录成功的时候https://www.jd.com/?newToken=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.gjB1lMc9cuyPsVNrkHwrpm34GRMLPzjJwl4YrX4ZVfY
        String token = request.getParameter("newToken");
        if(token != null){
            //1.将token放入cookie中、
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        //当用户登录成功之后，那么用户是否可以继续访问其他业务
        //是 商品检索: http://list.gmall.com/list.html?catalog3Id=60
        if(token == null){
            token = CookieUtil.getCookieValue(request,"token",false);
        }
        //当token真正不为空的时候，解密用户昵称
        if(token != null){
            //解密token即可
            Map map = getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
            //保存到request作用域中
            request.setAttribute("nickName",nickName);
        }
        //获取用户访问的控制器上是否有 @LoginRequire注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //获取方法上的注解
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        //判断是否有注解
        if(methodAnnotation != null){
            //直接认证! 用户是否登录! http://passport.atguigu.com/verify?token=xxx&salt=xxx
            String salt = request.getHeader("X-forwarded-for");
            //远程调用!
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);
            if("success".equals(result)){
                //用户已经是登录状态
                Map map = getUserMapByToken(token);
                String userId = (String) map.get("userId");
                //保存到作用域
                request.setAttribute("userId",userId);
                //放行
                return true;
            }else{
                //当LoginRequire的注解为true时必须登录!
                if (methodAnnotation.autoRedirect()){
                    //应该跳转到登陆页 http:item.gmall.com/37.html ---> http://passport.atguigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F37.html
                    //需要先得到用户访问的url路径
                    String requestUrl = request.getRequestURL().toString();
                    System.out.println(requestUrl);//http:item.gmall.com/37.html
                    //将http:item.gmall.com/37.html --> http%3A%2F%2Fitem.gmall.com%2F37.html
                    String encode = URLEncoder.encode(requestUrl, "UTF-8");
                    System.out.println(encode);
                    //重定向
                    response.sendRedirect(WebConst.LOGIN_ADDRESS + "?originUrl=" + encode);
                    //拦截
                    return false;
                }
            }
        }
        return true;
    }
    //解密token
    private Map getUserMapByToken(String token) {
        //token = eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.gjB1lMc9cuyPsVNrkHwrpm34GRMLPzjJwl4YrX4ZVfY
        //解密得到token的中间部分
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        System.out.println(tokenUserInfo);//eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0
        //通过 Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] bytes = base64UrlCodec.decode(tokenUserInfo);
        //byte -> String -> Map
        String tokenJson = new String(bytes);
        return JSON.parseObject(tokenJson,Map.class);
    }


    //进入控制器后，返回视图之前执行
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }
    //视图渲染之后执行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
