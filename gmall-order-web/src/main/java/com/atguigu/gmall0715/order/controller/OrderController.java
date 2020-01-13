package com.atguigu.gmall0715.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.CartInfo;
import com.atguigu.gmall0715.bean.OrderDetail;
import com.atguigu.gmall0715.bean.OrderInfo;
import com.atguigu.gmall0715.bean.UserAddress;
import com.atguigu.gmall0715.bean.enums.OrderStatus;
import com.atguigu.gmall0715.bean.enums.ProcessStatus;
import com.atguigu.gmall0715.config.CookieUtil;
import com.atguigu.gmall0715.config.LoginRequire;
import com.atguigu.gmall0715.service.CartService;
import com.atguigu.gmall0715.service.OrderService;
import com.atguigu.gmall0715.service.UserAddressService;
import com.atguigu.gmall0715.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
public class OrderController {

    @Reference
    private UserService userService;
    @Reference
    private CartService cartService;
    @Reference
    private OrderService orderService;

    @RequestMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request, HttpServletResponse response){//List<UserAddress>
        String userId = (String) request.getAttribute("userId");
        List<UserAddress> userAddressList = userService.findUserAddress(userId);
        //必须获取购物车选中的数据
        List<CartInfo> cartInfoList = cartService.getCartCheckedList(userId);
        //第一种: 页面直接渲染cartInfoList
        //第二种: 页面渲染orderDetail
        //声明一个集合来存储订单明细
        ArrayList<OrderDetail> detailArrayList = new ArrayList<OrderDetail>();
        for (CartInfo cartInfo : cartInfoList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            //添加订单明细
            detailArrayList.add(orderDetail);
        }

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();
        //保存数据

        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        request.setAttribute("detailArrayList",detailArrayList);
        request.setAttribute("userAddressList",userAddressList);
        return "trade";
     }

     // http://trade.gmall.com/submitOrder
    @RequestMapping("submitOrder")
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        //1.将数据添加到数据库表中! cartInfo orderDetail!
            //调用服务层保存

        String userId = (String) request.getAttribute("userId");
        orderInfo.setUserId(userId);

        String outTradeNo = "ATGUIGU" + System.currentTimeMillis()+"" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);

        String orderId = orderService.saveOrderInfo(orderInfo);
        //2.判断后台如何接收前台传递过来的数据

        //重定向到支付模块
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }
}
