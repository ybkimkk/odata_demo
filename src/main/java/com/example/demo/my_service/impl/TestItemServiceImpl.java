package com.example.demo.my_service.impl;

import cn.hutool.core.convert.Convert;
import com.example.demo.entity.TestItemEntity;
import com.example.demo.mapper.TestItemMapper;
import com.example.demo.my_service.ITestItemService;
import com.example.demo.my_service.common.AbCommonService;
import lombok.RequiredArgsConstructor;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author jinyongbin
 * @since 2024-03-21 11:57:19
 */

@Service("TestItemService")
@RequiredArgsConstructor
public class TestItemServiceImpl extends AbCommonService implements ITestItemService {


    private final TestItemMapper testItemMapper;


    @Override
    public List<TestItemEntity> selectByCondition(Map<String, Object> arg) throws NullPointerException {
        TestItemEntity convert = Convert.convert(TestItemEntity.class, arg);
        return testItemMapper.selectByCondition(convert);
    }

    @Override
    public TestItemEntity insert(Map<String, Object> arg) throws NullPointerException {
        return null;
    }

    @Override
    public TestItemEntity update(Map<String, Object> arg) throws NullPointerException {
        return null;
    }

    @Override
    public int delete(String id) throws NullPointerException {
        return 0;
    }

    //多对一
    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(entitySetName);
        entitySet.setType(getAllFullQualifiedName(entitySetName));
        CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
        navPropBinding.setTarget("Test");
        navPropBinding.setPath("Test");
        entitySet.setNavigationPropertyBindings(Collections.singletonList(navPropBinding));
        return entitySet;
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName(entityTypeName.getName());
        List<CsdlProperty> csdlPropertyArrayList = new ArrayList<>();
        csdlPropertyArrayList.add(new CsdlProperty().setName("ID").setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName()));
        csdlPropertyArrayList.add(new CsdlProperty().setName("NAME").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()));
        csdlPropertyArrayList.add(new CsdlProperty().setName("TEST_ID").setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName()));
        entityType.setProperties(csdlPropertyArrayList);
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("ID");
        entityType.setKey(Collections.singletonList(propertyRef));

        //多对一
        CsdlNavigationProperty navProp = new CsdlNavigationProperty()
                .setName("Test") //传连接目标
                .setType(getAllFullQualifiedName("Test"))//传连接目标
                .setNullable(false)
                .setPartner("TestItem"); //传自己

        entityType.setNavigationProperties(Collections.singletonList(navProp));
        return entityType;
    }

    @Override
    public List<CsdlAction> getActions(final FullQualifiedName actionName) {
        return null;
    }

    @Override
    public CsdlActionImport getActionImport(FullQualifiedName entityContainer, String actionImportName) {
        return null;
    }

    @Override
    public List<CsdlFunction> getFunctions(FullQualifiedName functionName) {
        return null;
    }

    @Override
    public CsdlFunctionImport getFunctionImport(FullQualifiedName entityContainer, String functionImportName) {
        return null;
    }
}
