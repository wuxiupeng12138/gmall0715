package com.atguigu.gmall0715.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall0715.bean.*;
import com.atguigu.gmall0715.service.ListService;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;
    @Reference
    private ManageService manageService;

    //http://list.qmall.com/list.html?catalog3Id=285
    @RequestMapping("list.html")
    //@ResponseBody
    public String list(SkuLsParams skuLsParams, HttpServletRequest request){
        //设置每页显示的条数
        skuLsParams.setPageSize(2);

        //1.根据请求查询SkuLsResult数据
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        // return JSON.toJSONString(skuLsResult); 测试数据正确性
        //2.获取到所有的商品信息，并
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        //3.显示平台属性，平台属性值
        //必须得到平台属性值id
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        //根据平台属性值id查询平台属性集合
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrInfoList(attrValueIdList);

        //4.制作urlParam参数
        String urlParam =  makeUrlParam(skuLsParams);
        System.out.println("参数列表: " + urlParam);

        //5.平台属性值过滤
        //声明一个保存面包屑的集合
        ArrayList<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();
        if(baseAttrInfoList != null && baseAttrInfoList.size() > 0){
            for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo =  iterator.next();
                //获取平台属性值集合
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    if(skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
                        for (String valueId : skuLsParams.getValueId()) {
                            //http://list.qmall.com/list.html?valueId=37 中的valueId与attrValueList中的平台属性值id进行比较
                            if(baseAttrValue.getId().equals(valueId)){
                                iterator.remove();
                                //6.生成面包屑： 平台属性名称、平台属性值名称
                                //将面包屑的内容 赋值给了平台属性值对象的名称
                                BaseAttrValue baseAttrValued = new BaseAttrValue();
                                baseAttrValued.setValueName(baseAttrInfo.getAttrName() + ":" + baseAttrValue.getValueName());
                                String newUrlParam = makeUrlParam(skuLsParams, valueId);
                                //赋值最新的请求参数列表
                                baseAttrValued.setUrlParam(newUrlParam);
                                //将每个面包屑都放入集合中!
                                baseAttrValueArrayList.add(baseAttrValued);
                            }
                        }
                    }
                }
            }
        }

        //分页
        request.setAttribute("pageNo",skuLsParams.getPageNo());
        request.setAttribute("totalPages",skuLsResult.getTotalPages());
        //请求地址
        request.setAttribute("urlParam",urlParam);
        //面包屑部分 存储着部分平台属性-平台属性值
        request.setAttribute("baseAttrValueArrayList",baseAttrValueArrayList);

        //保存数据到request域
        //显示的搜索名称-回显作用
        request.setAttribute("keyword",skuLsParams.getKeyword());
        //显示的商品信息
        request.setAttribute("skuLsInfoList",skuLsInfoList);
        //显示的平台属性值平台属性集合
        request.setAttribute("baseAttrInfoList",baseAttrInfoList);
        //跳转页面
        return "list";
    }

    //制作查询的参数
    private String makeUrlParam(SkuLsParams skuLsParams,String ... excludeValueIds) {
        String urlParam = "";
        //判断用户是否输入的是keyword!
        //http://list.qmall.com/list.html?keyword=手机
        if(skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length() > 0){
            urlParam += "keyword=" + skuLsParams.getKeyword();
        }
        //判断用户输入的是否是三级分类id
        //http://list.qmall.com/list.html?catalog3Id=285
        if(skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0){
            urlParam += "catalog3Id=" + skuLsParams.getCatalog3Id();
        }
        //判断用户是否输入的平台属性值id检索条件
        //http://list.qmall.com/list.html?valueId=37
        if(skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
            for (String valueId : skuLsParams.getValueId()) {
                if(excludeValueIds != null && excludeValueIds.length > 0){
                    //获取对象中的第一个数据,若是面包屑点击请求的不进行拼接
                    String excludeValueId = excludeValueIds[0];
                    if(excludeValueId.equals(valueId)){//不进行拼接
                        continue;
                    }
                }
                if (urlParam.length() > 0){
                    urlParam += "&";
                }
                urlParam += "valueId=" + valueId;

            }
        }
        return urlParam;
    }
}

