package com.atguigu.gmall0715.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.*;
import com.atguigu.gmall0715.bean.enums.OrderStatus;
import com.atguigu.gmall0715.bean.enums.ProcessStatus;
import com.atguigu.gmall0715.config.CookieUtil;
import com.atguigu.gmall0715.config.LoginRequire;
import com.atguigu.gmall0715.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

@Controller
public class OrderController {

    @Reference
    private UserService userService;
    @Reference
    private CartService cartService;
    @Reference
    private OrderService orderService;
    @Reference
    private ManageService manageService;

    /**
     *  生成订单界面
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request, HttpServletResponse response){//List<UserAddress>
        //1.获取userId，并查询地址信息
        String userId = (String) request.getAttribute("userId");
        List<UserAddress> userAddressList = userService.findUserAddress(userId);
        //2.必须获取购物车选中的数据
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
        /**
         * 生成流水号，保证不会重复下单
         */
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);
        //保存数据
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        request.setAttribute("detailArrayList",detailArrayList);
        request.setAttribute("userAddressList",userAddressList);

        return "trade";
     }

     // http://trade.gmall.com/submitOrder
    /**
     *  提交订单
     * @param orderInfo
     * @param request
     * @return
     */
    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        //1.将数据添加到数据库表中! cartInfo orderDetail!
            //调用服务层保存
        String userId = (String) request.getAttribute("userId");
        orderInfo.setUserId(userId);

        String outTradeNo = "ATGUIGU" + System.currentTimeMillis()+"" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //防止表单重复提交
        //获取页面的流水号
        String tradeNo = request.getParameter("tradeNo");
        //调用比较方法
        boolean result = orderService.checkTradeNo(tradeNo, userId);
        //验证失败!
        if(!result){
            request.setAttribute("errMsg","请勿重复提交订单");
            return "tradeFail";
        }
        //删除缓存的流水号!
        orderService.delTradeNo(userId);

        //验证库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            boolean flag = orderService.checkStock(orderDetail.getSkuId(),orderDetail.getSkuNum());
            if(!flag){
                request.setAttribute("errMsg",orderDetail.getSkuName() + "库存不足，请联系客服!");
                return "tradeFail";
            }
            //验证价格orderDetail.getOrderPrice() == skuInfo.price
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            int res = orderDetail.getOrderPrice().compareTo(skuInfo.getPrice());
            if(res != 0){
                request.setAttribute("errMsg",orderDetail.getSkuName() + "商品价格有变动，请重新下单!");
                //加载最新价格到缓存
                cartService.loadCartCache(userId);
                return "tradeFail";
            }
        }
        String orderId = orderService.saveOrderInfo(orderInfo);
        //2.判断后台如何接收前台传递过来的数据

        //重定向到支付模块
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }
}
