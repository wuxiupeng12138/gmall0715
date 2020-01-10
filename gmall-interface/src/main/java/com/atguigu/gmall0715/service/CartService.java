package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.CartInfo;

import java.util.List;

public interface CartService {

    /**
     * 添加购物车
     * @param skuId
     * @param skuNum
     * @param userId
     */
    void addToCart(String skuId,Integer skuNum,String userId);

    /**
     * 根据用户id查询购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);
}
