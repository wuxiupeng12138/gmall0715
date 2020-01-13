package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.OrderInfo;

public interface OrderService {

    /**
     * 订单接口
     * @param orderInfo
     * @return
     */
    String saveOrderInfo(OrderInfo orderInfo);
}
