package com.atguigu.gmall0715.order.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0715.bean.OrderDetail;
import com.atguigu.gmall0715.bean.OrderInfo;
import com.atguigu.gmall0715.bean.enums.OrderStatus;
import com.atguigu.gmall0715.bean.enums.ProcessStatus;
import com.atguigu.gmall0715.order.mapper.OrderDetailMapper;
import com.atguigu.gmall0715.order.mapper.OrderInfoMapper;
import com.atguigu.gmall0715.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMappers;

    @Autowired
    private OrderDetailMapper orderDetailMapper;


    @Override
    @Transactional
    public String saveOrderInfo(OrderInfo orderInfo) {
        // 订单的总金额、订单的状态、用户id、第三方交易编号、创建时间、过期时间、进程状态也没有!
        orderInfo.sumTotalAmount();
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        //创建时间
        orderInfo.setCreateTime(new Date());
        //设置过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);

        orderInfo.setExpireTime(calendar.getTime());
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);


        //两个表 orderInfo、orderDetail
        orderInfoMappers.insertSelective(orderInfo);
        //orderDetail
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //循环遍历
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setId(null);
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        //返回订单编号
        return orderInfo.getId();
    }
}
