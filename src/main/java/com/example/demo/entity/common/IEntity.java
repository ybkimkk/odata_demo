package com.example.demo.entity.common;

import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;

public interface IEntity {

    /**
     * ODATA 实体类声明
     */
    CsdlEntityType getEntityType();

    /**
     * ODATA 实例化声明
     */
    CsdlEntitySet getEntitySet();
}
