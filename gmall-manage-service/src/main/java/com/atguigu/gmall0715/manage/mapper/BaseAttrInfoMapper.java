package com.atguigu.gmall0715.manage.mapper;

import com.atguigu.gmall0715.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    /**
     * 根据三级分类id进行多表关联查询
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> selectBaseAttrInfoListByCatalog3Id(String catalog3Id);

    /**
     * 通过平台属性值id查询平台属性集合
     * @param valueIds
     * @return
     */
    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("valueIds") String valueIds);
}
