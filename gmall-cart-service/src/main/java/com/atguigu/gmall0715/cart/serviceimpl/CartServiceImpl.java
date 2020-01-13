package com.atguigu.gmall0715.cart.serviceimpl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.CartInfo;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.cart.constant.CartConst;
import com.atguigu.gmall0715.cart.mapper.CartInfoMapper;
import com.atguigu.gmall0715.config.RedisUtil;
import com.atguigu.gmall0715.service.CartService;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Reference
    private ManageService manageService;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void addToCart(String skuId, Integer skuNum, String userId) {

        //2.添加完成之后，必须更新redis
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //key = user:userId:cart   field = skuId  value:
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        if(!jedis.exists(cartKey)){
            //购物车key 不存在时，加载数据到缓存!
            loadCartCache(userId);
        }
        //1.判断购物车中是否有要添加的该商品 true：数量相加 false: 直接添加数据 {mysql --- redis}
        //select * from cartInfo where skuId = ? and userId = ?
//        CartInfo cartInfo = new CartInfo();
//        cartInfo.setUserId(userId);
//        cartInfo.setSkuId(skuId);
//        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userId).andEqualTo("skuId",skuId);
        CartInfo cartInfoExist = cartInfoMapper.selectOneByExample(example);

        //根据skuId查询skuInfo商品信息
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        if(cartInfoExist != null){
            //说明购物车中有此商品! 数量应该相加
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() +skuNum);
            //初始化skuProce
            //cartInfoExist.setSkuPrice(cartInfoExist.getSkuPrice());
            cartInfoExist.setSkuPrice(skuInfo.getPrice());
            //更新数据库
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        }else{
            //添加数据的来源
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuNum(skuNum);
            cartInfo1.setSkuId(skuId);
            cartInfo1.setUserId(userId);
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setSkuName(skuInfo.getSkuName());

            cartInfoMapper.insertSelective(cartInfo1);

            //为了不浪费gc
            cartInfoExist = cartInfo1;
        }
        jedis.hset(cartKey,skuId, JSON.toJSONString(cartInfoExist));
        //设置过期时间
        setCartKeyExpire(userId, jedis, cartKey);
        jedis.close();
    }

    /**
     * 1.先获取缓存中的数据
     *  true: 直接查询并返回集合
     *  false: 查询数据库，将数据放入缓存，并返回集合
     *      涉及到查询一下实时价格: skuInfo.price
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //redis更新 数据类型: jedis.hset(key,field,value);
        //key = user:userId:cart   field = skuId  value:
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //hash 获取数据
        List<String> stringList = jedis.hvals(cartKey);
        //循环遍历获取数据
        if (stringList != null && stringList.size() > 0){
            for (String cartInfoJson : stringList) {
                //将缓存中的cartInfo添加到集合
                cartInfoList.add(JSON.parseObject(cartInfoJson,CartInfo.class));
            }
            //        //自定义比较器
//        cartInfoList.sort(new Comparator<CartInfo>() {
//            @Override
//            public int compare(CartInfo o1, CartInfo o2) {
//                return o1.getId().compareTo(o2.getId());
//            }
//        });
            //流式编程: 开发经常用
            Collections.sort(cartInfoList,(CartInfo o1, CartInfo o2)->o1.getId().compareTo(o2.getId()));

            return cartInfoList;
        }else{
            //走db 放入缓存
            cartInfoList = loadCartCache(userId);
            return cartInfoList;
        }
    }

    @Override
    public List<CartInfo> mergerToCartList(List<CartInfo> cartInfoNoLoginList, String userId) {
        //合并之后的集合
        List<CartInfo> cartInfoList = new ArrayList<>();
        //登录 + 未登录
        List<CartInfo> cartInfoLoginList  = cartInfoMapper.selectCartListWithCurPrice(userId);
        //准备做合并工作
        if(cartInfoLoginList != null && cartInfoLoginList.size() > 0){
            //循环判断  合并的条件: skuId商品id
            for (CartInfo cartInfoNoLogin : cartInfoNoLoginList) {
                //声明一个标识来判断是否有相同的商品
                boolean isMatch = false;
                for (CartInfo cartInfoLogin : cartInfoLoginList) {
                    if(cartInfoLogin.getSkuId().equals(cartInfoNoLogin.getSkuId())){
                        //数量相加
                        cartInfoLogin.setSkuNum(cartInfoNoLogin.getSkuNum() + cartInfoLogin.getSkuNum());
                        //更新到数据库mysql
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoLogin);
                        //操作缓存！

                        //当有相同的商品时修改即可
                        isMatch = true;
                    }
                }
                //没有相同的商品
                if(!isMatch){
                    //直接将未登录的数据添加到登录数据
                    //防止从数据库查询出来的id重复插入
                    cartInfoNoLogin.setId(null);
                    //设置登录的userId给未登录的对象
                    cartInfoNoLogin.setUserId(userId);
                    cartInfoMapper.insertSelective(cartInfoNoLogin);
                    //操作缓存1
                }
            }
        }else{
            //登录的时候没有数据，未登录的数据直接改成登录的数据
            for (CartInfo cartInfo : cartInfoNoLoginList) {
                //防止从数据库查询出来的id重复插入
                cartInfo.setId(null);
                //设置登录的userId给未登录的对象
                cartInfo.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfo);
                //操作缓存!
            }
        }
        //做合并之后的汇总 {操作了缓存}
        cartInfoList = loadCartCache(userId);
        //判断状态合并购物车
        if(cartInfoList != null && cartInfoList.size() > 0){
            for (CartInfo cartInfo : cartInfoList) {
                for (CartInfo info : cartInfoNoLoginList) {
                    //保存商品状态相同
                    if (info.getSkuId().equals(cartInfo.getSkuId())) {
                        //判断选择状态 根据未登录
                        if("1".equals(info.getIsChecked())){
                            //更改数据库的状态
                            cartInfo.setIsChecked("1");
                            //调用选中的方法
                            checkCart(info.getSkuId(),userId,"1");

                        }
                    }
                }
            }
        }

        return cartInfoList;
    }

    @Override
    public void deleteCartList(String userTempId) {
        //删除未登录购物车数据: redis + mysql
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userTempId);
        cartInfoMapper.deleteByExample(example);
        //1.删除缓存
        Jedis jedis = redisUtil.getJedis();
        //key = user:userId:cart   field = skuId  value:
        String cartKey = CartConst.USER_KEY_PREFIX + userTempId + CartConst.USER_CART_KEY_SUFFIX;
        jedis.del(cartKey);
        jedis.close();


    }

    @Override
    public void checkCart(String skuId, String userId, String isChecked) {
        //修改: mysql -- redis
        //方案一: 修改mysql - redis
        //方案二: 修改完成，删除缓存，最新数据添加到缓存
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userId).andEqualTo("skuId",skuId);
        //第一个参数标识修改的内容，第二个参数example永远都代表查询、更新、删除等...的条件
        cartInfoMapper.updateByExampleSelective(cartInfo,example);
        //修改缓存 使用的hash!
        Jedis jedis = redisUtil.getJedis();
        //key = user:userId:cart   field = skuId  value:
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //获取当当前的商品
        String cartInfoJson = jedis.hget(cartKey, skuId);
        //转换为对象
        CartInfo cartInfoUpd = JSON.parseObject(cartInfoJson, CartInfo.class);
        cartInfoUpd.setIsChecked(isChecked);
        jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfoUpd));

        //关闭jedis
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        //直接获取缓存数据
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //redis更新 数据类型: jedis.hset(key,field,value);
        //key = user:userId:cart   field = skuId  value:
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //获取所有数据
        List<String> stringList = jedis.hvals(cartKey);
        if(stringList != null && stringList.size() > 0){
            for (String cartInfoJson : stringList) {
                //cartInfoJson 转换为 cartInfo
                CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
                //获取到被选中的商品
                if ("1".equals(cartInfo.getIsChecked())) {
                    cartInfoList.add(cartInfo);
                }
            }
        }
        //关闭jedis
        jedis.close();

        return cartInfoList;
    }

    /**
     * 根据用户id查询数据库并放入缓存
     * @param userId
     * @return
     */
    private List<CartInfo> loadCartCache(String userId) {
        //查询最新数据给缓存! 其实就是商品的实时价格 skuInfo.price
        List<CartInfo> cartInfoList  = cartInfoMapper.selectCartListWithCurPrice(userId);
        //判断用户购物车是否在数据库存在数据
        if(cartInfoList == null || cartInfoList.size() == 0){
            return null;
        }
        //循环遍历数据并将数据添加到缓存!
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        //redis更新 数据类型: jedis.hset(key,field,value);
        //key = user:userId:cart   field = skuId  value:
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        HashMap<String, String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            //jedis.hset(cartKey,cartInfo.getSkuId(),JSON.toJSONString(cartInfo))
            map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        jedis.hmset(cartKey,map);
        jedis.close();
        return cartInfoList;
    }

    private void setCartKeyExpire(String userId, Jedis jedis, String cartKey) {
        //设置过期时间 key = {用户的购买量 | 根据用户过期时间设置购物车的过期时间}
        //获取用户的key
        String userKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
        if(jedis.exists(userKey)){
            //获取用户key的过期时间
            Long ttl = jedis.ttl(userKey);
            //将用户的过期时间给购物车的过期时间
            jedis.expire(cartKey,ttl.intValue());
        }else{
            //给购物车一个默认的过期时间
            jedis.expire(cartKey,7*24*3600);
        }
    }
}
