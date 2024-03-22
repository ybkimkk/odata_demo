/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.example.demo.processor.common;

import cn.hutool.core.collection.CollUtil;
import com.example.demo.anotation.OdataAction;
import com.example.demo.anotation.OdataFunction;
import com.example.demo.contains.Contains;
import com.example.demo.service.common.ICommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
 * this class is supposed to declare the metadata of the OData service
 * it is invoked by the Olingo framework e.g. when the metadata document of the service is invoked
 * e.g. http://localhost:8080/ExampleService1/ExampleService1.svc/$metadata
 */
@Component
@Slf4j
public class InitEdmProvider extends CsdlAbstractEdmProvider {

    public static final String NAMESPACE = "OData.Demo";

    // EDM Container
    public static final String CONTAINER_NAME = "Container";
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    // Entity Types Names
    public static final String ET_PRODUCT_NAME = "Product";
    public static final FullQualifiedName ET_PRODUCT_FQN = new FullQualifiedName(NAMESPACE, ET_PRODUCT_NAME);

    public static final String ET_CATEGORY_NAME = "Category";
    public static final FullQualifiedName ET_CATEGORY_FQN = new FullQualifiedName(NAMESPACE, ET_CATEGORY_NAME);

    // Entity Set Names
    public static final String ES_PRODUCTS_NAME = "Products";
    public static final String ES_CATEGORIES_NAME = "Categories";

    // Action
    public static final String ACTION_RESET = "Reset";
    public static final FullQualifiedName ACTION_RESET_FQN = new FullQualifiedName(NAMESPACE, ACTION_RESET);

    //Bound Action
    public static final String ACTION_PROVIDE_DISCOUNT = "DiscountProducts";
    public static final FullQualifiedName ACTION_PROVIDE_DISCOUNT_FQN = new FullQualifiedName(NAMESPACE, ACTION_PROVIDE_DISCOUNT);

    //Bound Action
    public static final String ACTION_PROVIDE_DISCOUNT_FOR_PRODUCT = "DiscountProduct";
    public static final FullQualifiedName ACTION_PROVIDE_DISCOUNT_FOR_PRODUCT_FQN = new FullQualifiedName(NAMESPACE, ACTION_PROVIDE_DISCOUNT_FOR_PRODUCT);

    // Function
    public static final String FUNCTION_COUNT_CATEGORIES = "CountCategories";
    public static final FullQualifiedName FUNCTION_COUNT_CATEGORIES_FQN
            = new FullQualifiedName(NAMESPACE, FUNCTION_COUNT_CATEGORIES);
    //Bound Function
    public static final String FUNCTION_PROVIDE_DISCOUNT = "GetDiscountProducts";
    public static final FullQualifiedName FUNCTION_PROVIDE_DISCOUNT_FQN = new FullQualifiedName(NAMESPACE, FUNCTION_PROVIDE_DISCOUNT);

    public static final String FUNCTION_PROVIDE_DISCOUNT_FOR_PRODUCT = "GetDiscountProduct";
    public static final FullQualifiedName FUNCTION_PROVIDE_DISCOUNT_FOR_PRODUCT_FQN = new FullQualifiedName(NAMESPACE, FUNCTION_PROVIDE_DISCOUNT_FOR_PRODUCT);

    // Function/Action Parameters
    public static final String PARAMETER_AMOUNT = "Amount";

    //Bound Action Binding Parameter
    public static final String PARAMETER_CATEGORY = "ParamCategory";

    //Bound Function Binding Parameter
    public static final String PARAMETER_BIND = "BindingParameter";
//
//    public static final FullQualifiedName CONTAINER = new FullQualifiedName(Contains.NAME_SPACE, Contains.CONTAINER_NAME);

    @Resource
    private CommonProcessor commonProcessor;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public List<CsdlSchema> getSchemas() {
        try {
            // create Schema
            CsdlSchema schema = new CsdlSchema();
            schema.setNamespace(Contains.NAME_SPACE);
            CsdlEntityContainer entityContainer = new CsdlEntityContainer();
            entityContainer.setName(Contains.NAME_SPACE);

            List<CsdlEntityType> entityTypes = new ArrayList<>();
            List<CsdlAction> actions = new ArrayList<>();
            List<CsdlFunction> functions = new ArrayList<>();


            Map<String, ICommonService<?>> serviceMap = commonProcessor.getServiceMap();
            for (Map.Entry<String, ICommonService<?>> stringICommonServiceEntry : serviceMap.entrySet()) {
                String entityName = stringICommonServiceEntry.getKey();
                // add EntityTypes
                entityTypes.add(getEntityType(new FullQualifiedName(Contains.NAME_SPACE, entityName)));
//                // add actions
                List<CsdlAction> actionList = getActions(new FullQualifiedName(Contains.NAME_SPACE, entityName));
                if (Objects.nonNull(actionList)) {
                    actions.addAll(actionList);
                }
                //  add functions
                List<CsdlFunction> functionList = getFunctions(new FullQualifiedName(Contains.NAME_SPACE, entityName));
                if (Objects.nonNull(functionList)) {
                    functions.addAll(functionList);
                }


            }
            schema.setEntityTypes(entityTypes);
            if (CollUtil.isNotEmpty(actions)) {
                schema.setActions(actions);
            }
            if (CollUtil.isNotEmpty(functions)) {
                schema.setFunctions(functions);
            }


            // add EntityContainer

            schema.setEntityContainer(getEntityContainer());


            List<CsdlSchema> schemas = new ArrayList<>();
            schemas.add(schema);
            return schemas;
        } catch (Exception e) {
            log.error("InitEdmProvider.getSchemas has error msg:", e);
        }
        return null;
    }

    public CsdlEntityContainer getEntityContainer() {
        try {
            CsdlEntityContainer entityContainer = new CsdlEntityContainer();

            List<CsdlEntitySet> entitySets = new ArrayList<>();

            List<CsdlFunctionImport> functionImports = new ArrayList<>();
            List<CsdlActionImport> actionImports = new ArrayList<>();


            Map<String, ICommonService<?>> serviceMap = commonProcessor.getServiceMap();
            for (Map.Entry<String, ICommonService<?>> stringICommonServiceEntry : serviceMap.entrySet()) {
                String entityName = stringICommonServiceEntry.getKey();
                Object bean = applicationContext.getBean(entityName + "Service");
                Class<?> clazz = bean.getClass();
                Method[] methods = clazz.getDeclaredMethods();
                // add EntitySets
                entitySets.add(getEntitySet(CONTAINER, entityName));

                for (Method method : methods) {
                    //  add function imports
                    if (method.isAnnotationPresent(OdataFunction.class)) {
                        String methodName = method.getName();
                        CsdlFunctionImport functionImport = getFunctionImport(new FullQualifiedName(Contains.NAME_SPACE, entityName), methodName);
                        if (Objects.nonNull(functionImport)) {
                            functionImports.add(functionImport);
                        }
                    }

                    // add action imports
                    if (method.isAnnotationPresent(OdataAction.class)) {
                        String methodName = method.getName();
                        CsdlActionImport actionImport = getActionImport(new FullQualifiedName(Contains.NAME_SPACE, entityName), methodName);
                        if (Objects.nonNull(actionImport)) {
                            actionImports.add(actionImport);
                        }
                    }

                }

            }

            entityContainer.setEntitySets(entitySets);
            if (CollUtil.isNotEmpty(functionImports)) {
                entityContainer.setFunctionImports(functionImports);
            }
            if (CollUtil.isNotEmpty(actionImports)) {
                entityContainer.setActionImports(actionImports);
            }
            entityContainer.setName(Contains.CONTAINER_NAME);


            return entityContainer;
        } catch (Exception e) {
            log.error("InitEdmProvider.getEntityContainer has error msg:", e);
        }
        return null;
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        return commonProcessor.getServiceMap().get(entityTypeName.getName()).getEntityType(entityTypeName);
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
        return commonProcessor.getServiceMap().get(entitySetName).getEntitySet(entityContainer, entitySetName);
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {
        CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
        entityContainerInfo.setContainerName(CONTAINER);
        return entityContainerInfo;
    }


    @Override
    public List<CsdlAction> getActions(final FullQualifiedName actionName) {
        return commonProcessor.getServiceMap().get(actionName.getName()).getActions(actionName);
    }

    @Override
    public CsdlActionImport getActionImport(final FullQualifiedName entityContainer, String actionImportName) {
        return commonProcessor.getServiceMap().get(entityContainer.getName()).getActionImport(entityContainer, actionImportName);
    }

    @Override
    public List<CsdlFunction> getFunctions(final FullQualifiedName functionName) {
        return commonProcessor.getServiceMap().get(functionName.getName()).getFunctions(functionName);
    }

    @Override
    public CsdlFunctionImport getFunctionImport(final FullQualifiedName entityContainer, String functionImportName) {
        return commonProcessor.getServiceMap().get(entityContainer.getName()).getFunctionImport(entityContainer, functionImportName);
    }

}
