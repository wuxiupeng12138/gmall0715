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
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 总共四步 :
 *      1.添加购物车
 *          查询购物车内信息
 *      2.查询购物车
 *      3.修改勾选状态
 *      4.生成订单
 */
@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Reference
    private ManageService manageService;

    /**
     * 添加购物车
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("addToCart") //http://cart.gmall.com/addToCart
    @LoginRequire(autoRedirect = false)//使拦截器生效
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        //1.得到前台传递过来的数据 {添加数量和商品id}
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


    /**
     * 查询购物车
     * @param request
     * @return
     */
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
            //先获取未登录的购物车数据
            String userTempId = CookieUtil.getCookieValue(request, "user-key", false);
            //声明一个集合来存储未登录购物车数据
            List<CartInfo> cartInfoNoLoginList = new ArrayList<>();
            //获取未登录购物车数据
            if(!StringUtils.isEmpty(userTempId)){
                cartInfoNoLoginList =  cartService.getCartList(userTempId);
                //判断集合中是否有数据
                if(cartInfoNoLoginList != null && cartInfoNoLoginList.size() > 0){
                    //开始合并购物车
                    cartInfoList = cartService.mergerToCartList(cartInfoNoLoginList,userId);
                    //删除未登录购物车数据
                    cartService.deleteCartList(userTempId);
                }
            }
            //什么情况直接查询登录数据
            if(StringUtils.isEmpty(userTempId) || (cartInfoNoLoginList == null || cartInfoNoLoginList.size() == 0)){
                //如果临时id为空，说明已登录，直接查询登录信息
                cartInfoList = cartService.getCartList(userId);
            }
        }else{
            //未登录，获取临时用户id 查询数据，临时用户id在cookie中
            String userTempId = CookieUtil.getCookieValue(request, "user-key", false);
            //获取未登录购物车数据
            if(!StringUtils.isEmpty(userTempId)){
                cartInfoList =  cartService.getCartList(userTempId);
            }
        }
        request.setAttribute("cartInfoList",cartInfoList);

        return "cartList";
    }

    /**
     * 修改勾选状态
     * @param request
     */
    //http://cart.gmall.com/checkCart
    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request){
        //登录 未登录
        //获取用户id
        String userId = (String) request.getAttribute("userId");
        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");
        if(userId == null){
            //未登录
            userId = CookieUtil.getCookieValue(request,"user-key",false);
            cartService.checkCart(skuId,userId,isChecked);
        }
        //登录的
        cartService.checkCart(skuId,userId,isChecked);
    }

    /**
     * 去结算
     * @return
     */
    //http://cart.gmall.com/toTrade
    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        //细节处理! 选中状态合并
        String userTempId = CookieUtil.getCookieValue(request,"user-key",false);
        if(!StringUtils.isEmpty(userTempId)){
            List<CartInfo> cartInfoNoLoginList =  cartService.getCartList(userTempId);
            //判断购物车集合中是否有数据
            if(cartInfoNoLoginList != null && cartInfoNoLoginList.size() > 0){
                //开始合并购物车
                cartService.mergerToCartList(cartInfoNoLoginList,userId);

                //删除未登录购物车数据
                cartService.deleteCartList(userTempId);
            }
        }
        //重定向到订单
        return "redirect://trade.gmall.com/trade";

    }
}
