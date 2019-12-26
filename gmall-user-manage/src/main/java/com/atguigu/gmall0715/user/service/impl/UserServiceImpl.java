package com.atguigu.gmall0715.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0715.bean.UserAddress;
import com.atguigu.gmall0715.bean.UserInfo;
import com.atguigu.gmall0715.service.UserService;
import com.atguigu.gmall0715.user.mapper.UserAddressMapper;
import com.atguigu.gmall0715.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserInfo> findAll() {
        return userMapper.selectAll();
    }

    @Override
    public List<UserAddress> findUserAddress(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return userAddressMapper.select(userAddress);
    }
}
