package com.atguigu.gmall0715.bean;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuLsInfo implements Serializable {

    String id;

    BigDecimal price;
    //商品的skuName
    String skuName;

    String catalog3Id;

    String skuDefaultImg;
    //默认热度排名
    Long hotScore=0L;
    // 保存平台属性值id
    List<SkuLsAttrValue> skuAttrValueList;

}
