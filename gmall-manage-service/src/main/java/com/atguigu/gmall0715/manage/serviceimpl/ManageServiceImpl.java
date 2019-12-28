package com.atguigu.gmall0715.manage.serviceimpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall0715.bean.*;
import com.atguigu.gmall0715.manage.mapper.*;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        if(baseAttrInfo.getId() == null && baseAttrInfo.getId().length() == 0){
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
        baseAttrInfo.setAttrValueList(this.getAttrValueList(baseAttrInfo.getId()));
        return baseAttrInfo;
    }
}
