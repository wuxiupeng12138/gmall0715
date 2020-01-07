package com.atguigu.gmall0715.service;

import com.atguigu.gmall0715.bean.*;

import java.util.List;

public interface ManageService {
    /**
     * 查询所有的一级分类
     * @return
     */
    List<BaseCatalog1> getCatalog1();

    /**
     * 根据二级分类的对象进行查询二级分类的集合
     * @param baseCatalog2
     * @return
     */
    List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2);

    /**
     * 据三级分类的对象进行查询三级分类的集合
     * @param baseCatalog3
     * @return
     */
    List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3);

    /**
     * 据三级分类的id进行查询平台属性的集合
     * @param baseAttrInfo
     * @return
     */
    List<BaseAttrInfo> getAttrInfoList(BaseAttrInfo baseAttrInfo);

    /**
     * 保存平台属性以及平台属性值
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 修改之前的平台属性值数据回显
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(String attrId);

    /**
     *  通过attrid查询baseAttrInfo
     * @param attrId
     * @return
     */
    BaseAttrInfo getBaseAttrInfo(String attrId);

    /**
     * 通过subinfo对象内的三级分类id，获取所有的商品属性
     * @param spuInfo
     * @return
     */
    List<SpuInfo> getSpuList(SpuInfo spuInfo);

    /**
     * 查询所有的销售属性
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存spuinfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据商品id获取商品所有的图片集合
     * @param spuId
     * @return
     */
    List<SpuImage> getSpuImageList(String spuId);

    /**
     * 通过三级分类id查询
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getAttrInfoList(String catalog3Id);

    /**
     * 通过spuid查询销售属性集合
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    /**
     * 保存商品信息
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 通过skuId查询商品信息(skuInfo)
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(String skuId);

    /**
     * 通过skuId、spuId查询销售属性集合
     * @param skuInfo
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    /**
     * 根据spuid查询sku与销售属性值的集合
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    /**
     * 根据平台属性值id查询平台属性集合
     * @param attrValueIdList
     * @return
     */
    List<BaseAttrInfo> getAttrInfoList(List<String> attrValueIdList);
}
