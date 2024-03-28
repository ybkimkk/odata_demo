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
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author jinyongbin
 * @since  2024-03-21 11:57:19
 */
@Component
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("test_item")
public class TestItemEntity extends CommonEntity implements IEntity {
    @TableId(type = IdType.AUTO)
    private Long ID;
    private String NAME;
    private Long TEST_ID;

    @Override
    public CsdlEntityType getEntityType() {

        List<CsdlProperty> csdlPropertyArrayList = new ArrayList<>();
        csdlPropertyArrayList.add(new CsdlProperty().setName("ID").setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName()));
        csdlPropertyArrayList.add(new CsdlProperty().setName("NAME").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()));
        csdlPropertyArrayList.add(new CsdlProperty().setName("TEST_ID").setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName()));

        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("ID");

        //多对一
        CsdlNavigationProperty navProp = new CsdlNavigationProperty()
                .setName("Test") //传连接目标
                .setType(OdataUtil.getAllFullQualifiedName("Test"))//传连接目标
                .setNullable(true)
                .setPartner("TestItem"); //传自己

        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("TestItem");
        entityType.setProperties(csdlPropertyArrayList);
        entityType.setKey(Collections.singletonList(propertyRef));
        entityType.setNavigationProperties(Collections.singletonList(navProp));
        return entityType;
    }

    @Override
    public CsdlEntitySet getEntitySet() {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName("TestItem");
        entitySet.setType(OdataUtil.getAllFullQualifiedName("TestItem"));
        CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
        navPropBinding.setTarget("Test");
        navPropBinding.setPath("Test");
        entitySet.setNavigationPropertyBindings(Collections.singletonList(navPropBinding));
        return entitySet;
    }

}

