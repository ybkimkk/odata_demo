package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.entity.common.CommonEntity;
import com.example.demo.entity.common.IEntity;
import com.example.demo.util.OdataUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author jinyongbin
 * @since 2024-03-19 13:35:48
 */

@Component
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("test")
public class TestEntity extends CommonEntity implements IEntity {
    @TableId(type = IdType.AUTO)
    private Long ID;
    private String NAME;
    private Long AGE;
    private String PRICE;
    private List<TestItemEntity> TEST_ITEM;

    @Override
    public CsdlEntityType getEntityType() {


        List<CsdlProperty> csdlPropertyArrayList = new ArrayList<>();
        csdlPropertyArrayList.add(new CsdlProperty().setName("ID").setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName()));
        csdlPropertyArrayList.add(new CsdlProperty().setName("NAME").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()));
        csdlPropertyArrayList.add(new CsdlProperty().setName("AGE").setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName()));
        csdlPropertyArrayList.add(new CsdlProperty().setName("PRICE").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()));

        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("ID");
        //一对多
        List<CsdlNavigationProperty> navPropList = new ArrayList<>();
        CsdlNavigationProperty navProp = new CsdlNavigationProperty()
                .setName("TEST_ITEM")
                .setType(OdataUtil.getAllFullQualifiedName("TEST_ITEM"))
                .setCollection(true)   //必须传
                .setPartner("TEST");
        navPropList.add(navProp);

        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("TEST");
        entityType.setKey(Collections.singletonList(propertyRef));
        entityType.setProperties(csdlPropertyArrayList);
        entityType.setNavigationProperties(navPropList);
        return entityType;
    }

    @Override
    public CsdlEntitySet getEntitySet() {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName("TEST");
        entitySet.setType(OdataUtil.getAllFullQualifiedName("TEST"));
        CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
        navPropBinding.setTarget("TEST_ITEM");
        navPropBinding.setPath("TEST_ITEM");
        entitySet.setNavigationPropertyBindings(Collections.singletonList(navPropBinding));
        return entitySet;
    }

}

