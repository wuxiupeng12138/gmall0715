package com.atguigu.gmall0715.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkuLsParams implements Serializable {
    //skuName
    String  keyword;
    //三级分类id
    String catalog3Id;

    //平台属性值id
    String[] valueId;
    //当前页
    int pageNo=1;
    //每页显示数
    int pageSize=20;
}
