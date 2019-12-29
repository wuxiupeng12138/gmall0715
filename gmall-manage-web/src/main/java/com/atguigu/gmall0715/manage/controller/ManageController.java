package com.atguigu.gmall0715.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.*;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
public class ManageController {

    @Reference
    private ManageService manageService;

    /**
     * 返回所有的一级分类数据:
     *      http://localhost:8082/getCatalog1
     * @return
     */
    @RequestMapping("getCatalog1")
    public List<BaseCatalog1> getCatalog1(){
        return manageService.getCatalog1();
    }

    /**
     *  根据一级分类的id进行查询二级分类的集合:
     *      http://localhost:8082/getCatalog2?catalog1Id
     * @param baseCatalog2
     * @return
     */
    @RequestMapping("getCatalog2")
    public List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2){
        return manageService.getCatalog2(baseCatalog2);
    }

    /**
     *  根据二级分类id进行查询三级分类的集合
     *      http://localhost:8082/getCatalog3?catalog2Id=3
     * @param baseCatalog3
     * @return
     */
    @RequestMapping("getCatalog3")
    public List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3){
        return manageService.getCatalog3(baseCatalog3);
    }

    //http://localhost:8082/attrInfoList?catalog3Id=61
    /**
     * 根据三级分类的id查询平台属性名称的集合
     * @param baseAttrInfo
     * @return
     */
    @RequestMapping("attrInfoList")
    public List<BaseAttrInfo> attrInfoList(BaseAttrInfo baseAttrInfo){
        return manageService.getAttrInfoList(baseAttrInfo);
    }


    //http://localhost:8082/saveAttrInfo

    /**
     * 保存平台属性以及平台属性值 && 修改平台属性值
     * @param baseAttrInfo
     */
    @RequestMapping("saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
    }

    //http://localhost:8082/getAttrValueList?attrId=100
    @RequestMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){
        //功能来讲:
        //return manageService.getAttrValueList(baseAttrValue);
        //业务来讲：
        BaseAttrInfo baseAttrInfo = manageService.getBaseAttrInfo(attrId);
        if(baseAttrInfo == null){
            return null;
        }
        return baseAttrInfo.getAttrValueList();
    }





}
