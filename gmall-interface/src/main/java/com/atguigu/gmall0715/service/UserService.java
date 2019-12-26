package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.UserAddress;
import com.atguigu.gmall0715.bean.UserInfo;

import java.util.List;

//属于业务层接口（操作用户的）
public interface UserService {

    /**
     *  查询所有用户
     * @return
     */
    List<UserInfo> findAll();

    /**
     *  根据用户id查询用户地址列表
     * @Param userId
     * @return
     */
    List<UserAddress> findUserAddress(String userId);
}
