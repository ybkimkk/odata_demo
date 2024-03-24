package com.example.demo.my_service.impl;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.anotation.OdataAction;
import com.example.demo.anotation.OdataFunction;
import com.example.demo.entity.TestEntity;
import com.example.demo.mapper.TestMapper;
import com.example.demo.my_service.ITestService;
import com.example.demo.my_service.common.AbCommonService;
import lombok.RequiredArgsConstructor;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author jinyongbin
 * @since 2024-03-19 13:35:48
 */

@Service("TestService")
@RequiredArgsConstructor
public class TestServiceImpl extends AbCommonService implements ITestService {

    private final TestMapper testMapper;

    @Override
    public List<TestEntity> selectByCondition(Map<String, Object> arg) throws NullPointerException {
        TestEntity testEntity = Convert.convert(TestEntity.class, arg);
        if (Objects.nonNull(testEntity.getOffset()) && Objects.isNull(testEntity.getCount())) {
            testEntity.setCount(testMapper.selectCount(new QueryWrapper<>()));
        }
        return testMapper.selectByCondition(testEntity);
    }

    @Override
    public TestEntity insert(Map<String, Object> arg) throws NullPointerException {
        TestEntity convert = Convert.convert(TestEntity.class, arg);
        testMapper.insert(convert);
        return convert;
    }

    @Override
    public TestEntity update(Map<String, Object> arg) throws NullPointerException {
        TestEntity convert = Convert.convert(TestEntity.class, arg);
        testMapper.updateBatch(Collections.singletonList(convert));
        return convert;
    }

    @Override
    public int delete(String id) throws NullPointerException {
        return testMapper.deleteById(id);
    }

    //一对多
    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(entitySetName);
        entitySet.setType(getAllFullQualifiedName(entitySetName));
        CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
        navPropBinding.setTarget("TestItem");
        navPropBinding.setPath("TestItem");
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
        csdlPropertyArrayList.add(new CsdlProperty().setName("AGE").setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName()));
        csdlPropertyArrayList.add(new CsdlProperty().setName("PRICE").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName()));
        entityType.setProperties(csdlPropertyArrayList);
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("ID");
        entityType.setKey(Collections.singletonList(propertyRef));
        //一对多
        List<CsdlNavigationProperty> navPropList = new ArrayList<>();
        CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("TestItem").setType(getAllFullQualifiedName("TestItem")).setCollection(true)   //必须传
                .setPartner("Test");
        navPropList.add(navProp);
        entityType.setNavigationProperties(navPropList);
        return entityType;
    }

    @Override
    public List<CsdlAction> getActions(final FullQualifiedName actionName) {
        //创建入参
        List<CsdlAction> actions = new ArrayList<>();
        actions.add(getTest());
        return actions;
    }

    @Override
    public CsdlActionImport getActionImport(FullQualifiedName entityContainer, String actionImportName) {
        return new CsdlActionImport()
                .setName(actionImportName)
                .setAction(entityContainer);
    }

    @Override
    public List<CsdlFunction> getFunctions(FullQualifiedName actionName) {
        List<CsdlFunction> functions = new ArrayList<>();
        functions.add(getTestFunction());
        return functions;
    }

    @Override
    public CsdlFunctionImport getFunctionImport(FullQualifiedName entityContainer, String functionImportName) {
        return new CsdlFunctionImport()
                .setName(functionImportName)
                .setFunction(getAllFullQualifiedName(functionImportName))
                .setEntitySet(entityContainer.getName())
                .setIncludeInServiceDocument(true);
    }

    @OdataFunction
    public CsdlFunction getTestFunction() {
        // Create the parameter for the function
        final CsdlParameter parameterAmount = new CsdlParameter();
        parameterAmount.setName("ID");
        parameterAmount.setNullable(false);
        parameterAmount.setType(EdmPrimitiveTypeKind.Int64.getFullQualifiedName());

        // Create the return type of the function
        final CsdlReturnType returnType = new CsdlReturnType();
        returnType.setCollection(true);
        returnType.setType(getAllFullQualifiedName("Test"));

        // Create the function
        final CsdlFunction function = new CsdlFunction();
        function.setName("getTestFunction").setParameters(Collections.singletonList(parameterAmount)).setReturnType(returnType);
        return function;
    }

    @OdataAction
    public CsdlAction getTest() {
        // Create parameters
        final List<CsdlParameter> parameters = new ArrayList<>();
        final CsdlParameter parameter = new CsdlParameter();
        parameter.setName("ID");
        parameter.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        parameters.add(parameter);

        // Create the Csdl Action
        final CsdlAction action = new CsdlAction();
        action.setName("getTest");
        action.setParameters(parameters);


        return action;
//        return action;
    }

}