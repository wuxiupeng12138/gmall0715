package com.atguigu.gmall0715.manage.serviceimpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.*;
import com.atguigu.gmall0715.config.RedisUtil;
import com.atguigu.gmall0715.manage.constant.ManageConst;
import com.atguigu.gmall0715.manage.mapper.*;
import com.atguigu.gmall0715.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired(required = false)
    private BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired(required = false)
    private BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired(required = false)
    private BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired(required = false)
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired(required = false)
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired(required = false)
    private SpuInfoMapper spuInfoMapper;
    @Resource
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    //从Spring容器中获取数据(redisUtil)
    @Autowired
    private RedisUtil redisUtil;


    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2) {
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3) {
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(BaseAttrInfo baseAttrInfo) {
        return baseAttrInfoMapper.select(baseAttrInfo);
    }


    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        if(baseAttrInfo.getId() == null){
            //说明当前时指向的添加
            //1.保存baseAttrInfo属性名称
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }else{
            //执行的是修改
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
            //如果执行的是修改先删除数据库中的已有的平台参数值
            BaseAttrValue baseAttrValue = new BaseAttrValue();
            baseAttrValue.setAttrId( baseAttrInfo.getId());
            baseAttrValueMapper.delete(baseAttrValue);
        }

        //2.从baseAttrInfo中取出属性值并存储到数据库中
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if(attrValueList != null && attrValueList.size() > 0){
            for (BaseAttrValue baseAttrValue:attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());//获取当前逐渐自增值
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public BaseAttrInfo getBaseAttrInfo(String attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        baseAttrInfo.setAttrValueList(getAttrValueList(attrId));
        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuList(SpuInfo spuInfo) {
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
        spuInfoMapper.insertSelective(spuInfo);
        //判断spuimage
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(spuImageList != null && spuImageList.size()>0){
            for (SpuImage spuImage : spuImageList) {
                //赋值spuid
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }
        //判断销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(spuSaleAttrList != null && spuSaleAttrList.size()>0){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);
                //销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if(spuSaleAttrValueList != null && spuSaleAttrValueList.size()>0){
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        //赋值spuid
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }
            }
        }

    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id) {
        //select * from base_attr_info bai
        //inner join base_attr_value bav
        //on bai.id = bav.attr_id
        //where bai.catalog3_id = ?
        return baseAttrInfoMapper.selectBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        //
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        //skuInfo
        skuInfoMapper.insertSelective(skuInfo);
        //skuAttrValue: 平台属性
        //获取出sku与平台属性的关系
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(skuAttrValueList != null && skuAttrValueList.size() > 0){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }
        //skuSaleAttrValue: 销售属性
        //获取出sku与销售属性的关系
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }

        }
        //skuImage: 图片表
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList != null && skuImageList.size() > 0){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {
        return getSkuInfoByRedisson(skuId);
        //return getSkuInfoByRedisSet(skuId);
    }

    private SkuInfo getSkuInfoByRedisson(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis = null;
        try {
            //1.获取jedis，创建Skuinfo对象
            jedis = redisUtil.getJedis();
            //2.定义key
            String skuKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFFIX;
            String skuJson = jedis.get(skuKey);
            if(skuJson == null){
                //redisson加锁，走数据库并放入缓存!
                Config config = new Config();
                //设置redisson节点
                config.useSingleServer().setAddress("redis://47.94.91.51:6379");
                //创建redisson实例
                RedissonClient redisson = Redisson.create(config);
                //创建锁
                RLock lock = redisson.getLock("myLock");
                System.out.println("redisson分布式锁!");
                //lock.lock(10, TimeUnit.SECONDS);
                //lock.lock();
                boolean res = lock.tryLock(100,10, TimeUnit.SECONDS);
                if(res){
                    try{
                        //业务逻辑
                        //从db中取数据并放入缓存
                        skuInfo = getSkuInfoDB(skuId);
                        if(skuInfo==null) {//解决的时缓存穿透的问题
                            jedis.setex(skuKey, ManageConst.SKUKEY_TIMEOUT, "");
                            return skuInfo;
                        }
                        //将数据放入缓存
                        jedis.setex(skuKey,ManageConst.SKUKEY_TIMEOUT, JSON.toJSONString(skuInfo));
                        return skuInfo;
                    }finally {
                        //解锁
                        lock.unlock();
                    }
                }
            }else{
                //缓存中有数据
                skuInfo = JSON.parseObject(skuJson,SkuInfo.class);
                return skuInfo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭缓存
            if(jedis!=null)
                jedis.close();
        }
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoByRedisSet(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis = null;
        try {
            //1.获取jedis，创建Skuinfo对象
            jedis = redisUtil.getJedis();
            //2.定义key
            String skuKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFFIX;
            String skuJson = jedis.get(skuKey);
            if(skuJson == null){
                //没有数据
                System.out.println("缓存中没有数据");
                //准备加索! set k1 v1 px 1000 nx
                //定义锁的key：k1
                String skuLockKey = ManageConst.SKUKEY_PREFIX+ skuId +ManageConst.SKUKEY_SUFFIX;
                //定义key锁定的值 v1
                String token = UUID.randomUUID().toString().replace("-","");
                //执行加索命令
                String lockKey = jedis.set(skuLockKey, token, "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if("OK".equals(lockKey)){
                    System.out.println("上锁成功");
                    //从db中取数据并放入缓存
                    skuInfo = getSkuInfoDB(skuId);
                    //将数据放入缓存
                    if(skuInfo == null){ //加此操作解决缓存穿透问题
                        jedis.setex(skuKey, ManageConst.SKUKEY_TIMEOUT, "");

                    }else{
                        jedis.setex(skuKey,ManageConst.SKUKEY_TIMEOUT, JSON.toJSONString(skuInfo));
                    }
                    //解锁 lua脚本
                    String script ="if redis.call('get', KEYS[1])==ARGV[1] then return redis.call('de1', EYS[1]) else return 0 end";
                    jedis.eval(script, Collections.singletonList(skuLockKey), Collections.singletonList(token));
                    return skuInfo;
                }else{
                    //说明有人正在操作!等待
                    Thread.sleep(1000);
                    return getSkuInfo(skuId);
                }
            }else{
                //缓存中有数据
                skuInfo = JSON.parseObject(skuJson,SkuInfo.class);
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭缓存
            if(jedis!=null)
                 jedis.close();
        }
        return getSkuInfoDB(skuId);
    }

    //抽取方法单独走数据库
    private SkuInfo getSkuInfoDB(String skuId) {
        //1.查询基本的skuInfo信息
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        //2.查询并封装skuImage到skuInfo内
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImages = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImages);

        //skuAttrValue
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        skuInfo.setSkuAttrValueList(skuAttrValueMapper.select(skuAttrValue));
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(),skuInfo.getSpuId());
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.getSkuSaleAttrValueListBySpu(spuId);
    }


}
