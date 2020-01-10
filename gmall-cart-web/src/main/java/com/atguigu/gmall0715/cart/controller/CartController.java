package com.atguigu.gmall0715.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.CartInfo;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.config.CookieUtil;
import com.atguigu.gmall0715.config.LoginRequire;
import com.atguigu.gmall0715.service.CartService;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Reference
    private ManageService manageService;

    //http://cart.gmall.com/addToCart
    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)//使拦截器生效
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        //得到前台传递过来的数据
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");
        //如何判断用户是否登陆
        String userId = (String) request.getAttribute("userId");
        if(userId == null){
            //用户未登陆 存储一个临时的用户id 存储在cookie中!
            userId = CookieUtil.getCookieValue(request, "user-key", false);
            //说明未登陆情况下，根本没有添加过一件商品
            if(userId == null){
                //起一个临时userId放入cookie中
                userId = UUID.randomUUID().toString().replace("-","");
                CookieUtil.setCookie(request,response,"user-key",userId,7*24*3600,false);
            }
        }
        cartService.addToCart(skuId,Integer.parseInt(skuNum),userId);
        //页面渲染使用
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "success";
    }
    //http://cart.gmall.com/cartList
    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request){
        //request.getParameter("");//获取浏览器url后面的参数，或者是表单提交过来的参数!
        //如何判断用户是否登陆
        String userId = (String) request.getAttribute("userId");//获取作用域中得数据
        List<CartInfo> cartInfoList = new ArrayList<>();
        //登陆的 获取数据时根据用户id去查询购物车列表
        if(userId != null){
            cartInfoList = cartService.getCartList(userId);
        }else{
            //未登录，获取临时用户id 查询数据，临时用户id在cookie中
            String userTempId = CookieUtil.getCookieValue(request, "user-key", false);
            //获取未登录购物车数据
            if(userTempId != null){
                cartInfoList =  cartService.getCartList(userTempId);
            }

        }


        request.setAttribute("cartInfoList",cartInfoList);

        return "cartList";
    }



}
