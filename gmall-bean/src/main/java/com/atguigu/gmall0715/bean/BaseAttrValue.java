package com.atguigu.gmall0715.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * 查询平台分类属性值
 */
@Data
public class BaseAttrValue implements Serializable {
    @Id
    @Column
    private String id;
    @Column
    private String valueName;
    @Column
    private String attrId;
    //单独的业务需要的字段
    @Transient
    private String urlParam;

}
