package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.PaymentInfo;

public interface PaymentService {

    /**
     * 保存记录{void}并生成二维码{返回页面}
     * @param paymentInfo
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 更新交易中的状态
     * @param out_trade_no paymentInfo
     */
    void updatePaymentInfo(String out_trade_no,PaymentInfo paymentInfo);

    /**
     * 根据outTradeNo查询数据
     * @param paymentInfo
     * @return
     */
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    /**
     * 退款业务
     * @param orderId
     * @return
     */
    boolean refund(String orderId);
}
