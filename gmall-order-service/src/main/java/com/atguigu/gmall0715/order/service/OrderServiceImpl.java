package com.atguigu.gmall0715.order.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0715.bean.OrderDetail;
import com.atguigu.gmall0715.bean.OrderInfo;
import com.atguigu.gmall0715.bean.enums.OrderStatus;
import com.atguigu.gmall0715.bean.enums.ProcessStatus;
import com.atguigu.gmall0715.config.RedisUtil;
import com.atguigu.gmall0715.order.mapper.OrderDetailMapper;
import com.atguigu.gmall0715.order.mapper.OrderInfoMapper;
import com.atguigu.gmall0715.service.OrderService;
import com.atguigu.gmall0715.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMappers;

    @Autowired
    private OrderDetailMapper orderDetailMapper;
    
    @Autowired
    private RedisUtil redisUtil;

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

    @Override
    public String getTradeNo(String userId) {
        //1.获取jedis
        Jedis jedis = redisUtil.getJedis();
        //2.确定数据类型
        String tradeNoKey = "user:"+userId+":tradeCode";
        //3.生成一个流水号
        String tradeCode = UUID.randomUUID().toString().replace("-","");
        //4.将流水号放入缓存
        jedis.set(tradeNoKey,tradeCode);
        //5.关闭jedis
        jedis.close();
        return tradeCode;
    }

    @Override
    public boolean checkTradeNo(String tradeNo, String userId) {
        //1.获取缓存中存储的流水号
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);

        //2.判断与页面隐藏域中的流水号是否一致
        jedis.close();
        return tradeNo.equals(tradeCode);
    }

    @Override
    public void delTradeNo(String userId) {
        //1.获取jedis
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        //2.删除reids中存储的流水号
        jedis.del(tradeNoKey);
        //为了避免用户重复操作，还可以使用lua脚本来进行删除
//        String script ="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//        jedis.eval(script, Collections.singletonList(tradeNoKey),Collections.singletonList(tradeCode));

        jedis.close();
    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        //远程调用库存接口方法 http://www.gware.com/hasStock?skuId=1022&num=2
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);

        return "1".equals(result);
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        return orderInfoMappers.selectByPrimaryKey(orderId);
    }
}
