package com.atguigu.gmall0715.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.bean.SpuImage;
import com.atguigu.gmall0715.bean.SpuSaleAttr;
import com.atguigu.gmall0715.bean.SpuSaleAttrValue;
import com.atguigu.gmall0715.service.ManageService;
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
    }
}
