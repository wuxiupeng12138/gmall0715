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

    /**
     * 合并购物车
     * @param cartInfoNoLoginList
     * @param userId
     * @return
     */
    List<CartInfo> mergerToCartList(List<CartInfo> cartInfoNoLoginList, String userId);

    /**
     * 删除未登录购物车信息
     * @param userTempId
     */
    void deleteCartList(String userTempId);

    /**
     *  修改购物车状态
     * @param skuId
     * @param userId
     * @param isChecked
     */
    void checkCart(String skuId, String userId, String isChecked);

    /**
     * 根据用户id查询购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);
}
