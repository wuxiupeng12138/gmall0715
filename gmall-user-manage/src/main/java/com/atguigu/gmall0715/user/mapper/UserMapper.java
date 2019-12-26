package com.atguigu.gmall0715.user.mapper;

import com.atguigu.gmall0715.bean.UserInfo;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

@Component
public interface UserMapper extends Mapper<UserInfo> {

}
