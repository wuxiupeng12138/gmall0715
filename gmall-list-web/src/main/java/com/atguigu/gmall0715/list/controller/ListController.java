package com.atguigu.gmall0715.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.SkuLsParams;
import com.atguigu.gmall0715.bean.SkuLsResult;
import com.atguigu.gmall0715.service.ListService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    //http://list.qmall.com/list.html?catalog3Id=285
    @RequestMapping("list.html")
    @ResponseBody
    public String list(SkuLsParams skuLsParams){
        SkuLsResult search = listService.search(skuLsParams);
        return JSON.toJSONString(search);
    }

}
