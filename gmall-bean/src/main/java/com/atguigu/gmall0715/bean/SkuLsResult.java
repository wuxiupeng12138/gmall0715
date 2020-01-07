package com.atguigu.gmall0715.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SkuLsResult implements Serializable {
    //商品的集合数据
    List<SkuLsInfo> skuLsInfoList;
    //总条数
    long total;
    //总页数
    long totalPages;
    //平台属性值id集合，显示平台属性和平台属性值
    List<String> attrValueIdList;
}
