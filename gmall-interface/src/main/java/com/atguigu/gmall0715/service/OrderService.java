package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.OrderInfo;

public interface OrderService {

    /**
     * 订单接口
     * @param orderInfo
     * @return
     */
    String saveOrderInfo(OrderInfo orderInfo);


    /**
     * 生成一个流水号
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 比较流水号
     * @param tradeNo
     * @param userId
     * @return
     */
    boolean checkTradeNo(String tradeNo,String userId);

    /**
     *  删除流水号
     * @param userId
     */
    void delTradeNo(String userId);

    /**
     *  验证库存： 查看库存是否够
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(String skuId, Integer skuNum);

    /**
     *  通过orderId查询orderInfo
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(String orderId);
}
