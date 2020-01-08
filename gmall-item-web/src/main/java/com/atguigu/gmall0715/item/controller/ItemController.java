package com.atguigu.gmall0715.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.SkuInfo;
import com.atguigu.gmall0715.bean.SkuSaleAttrValue;
import com.atguigu.gmall0715.bean.SpuSaleAttr;
import com.atguigu.gmall0715.config.LoginRequire;
import com.atguigu.gmall0715.service.ListService;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    private ManageService manageService;
    @Reference
    private ListService listService;

    @RequestMapping("{skuId}.html")
    @LoginRequire
    public String item(@PathVariable("skuId") String skuId, HttpServletRequest request){

        //1.通过skuId查询skuInfo，并保存skuInfo到request域中
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);

        //2.查询销售属性-销售属性值 并锁定 保存到作用域
        List<SpuSaleAttr> spuSaleAttrs = manageService.getSpuSaleAttrListCheckBySku(skuInfo);
        request.setAttribute("spuSaleAttrs",spuSaleAttrs);

        //3.查询销售属性值与skuId组合的数据集合
        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        //3.1 拼接字符串
        String key = "";
        HashMap<String, String> map = new HashMap<>();
        if(skuSaleAttrValueListBySpu != null && skuSaleAttrValueListBySpu.size() > 0){
            //{"135|138":"37",...}  key:"135|138" value:"37" map.put(key,value); map ---> Json
            //对应的拼接规则:
                // 1.如果skuId与下一个skuId不一致的时候，则停止拼接。
                // 2.当循环到集合末尾的时候，停止拼接
            for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
                SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
                //什么时候拼接/
                if(key.length() > 0){
                    key+="|";
                }
                //拼接key
                key += skuSaleAttrValue.getSaleAttrValueId();
                //什么时候停止拼接
                if ((i+1) == skuSaleAttrValueListBySpu.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i+1).getSkuId())){
                    map.put(key,skuSaleAttrValue.getSkuId());
                    //清空key
                    key="";
                }
            }
        }
        //将map转换为json
        String valueSkuJson = JSON.toJSONString(map);
        request.setAttribute("valueSkuJson",valueSkuJson);
        //记录热度排名
        listService.incrHotScore(skuId);
        return "item";
    }

}
