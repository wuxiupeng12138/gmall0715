package com.atguigu.gmall0715.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.*;
import com.atguigu.gmall0715.service.ListService;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class SkuManageController {

    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;


    //http://localhost:8082/spuImageList?spuId=61

    /**
     * 通过spuid查询所有的spu图
     * @param spuId
     * @return
     */
    @RequestMapping("spuImageList")
    public List<SpuImage> getSpuImageList(String spuId){
        return manageService.getSpuImageList(spuId);
    }

    //http://localhost:8082/spuSaleAttrList?spuId=60

    /**
     * 通过spuid查询销售属性 --- 销售属性值
     * @param spuId
     * @param spuSaleAttr
     * @return
     */
    @RequestMapping("spuSaleAttrList")
        public List<SpuSaleAttr> getSpuSaleAttrList(String spuId, SpuSaleAttr spuSaleAttr){
        return manageService.getSpuSaleAttrList(spuId);
    }
    //http://localhost:8082/saveSkuInfo
    @RequestMapping("saveSkuInfo")
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        //保存完成之后商品上架
        //发送消息队列异步处理! 通知管理员做审核，审核完成之后，商品上架(saveSkuLsInfo)
    }

    //如何上传? 根据skuId上传
        //批量上传 || 单个上传
    @RequestMapping("onSale")
    public void onSale(String skuId){
        //商品上架
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        //给skuLsInfo初始化赋值
        //根据skuId查询SkuInfo
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        //属性拷贝
        BeanUtils.copyProperties(skuInfo,skuLsInfo);

        listService.saveSkuLsInfo(skuLsInfo);
    }
}
