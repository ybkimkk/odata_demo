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
import com.alibaba.fastjson2.JSON;
import com.example.demo.anotation.OdataAction;
import com.example.demo.anotation.OdataFunction;
import com.example.demo.contains.Contains;
import com.example.demo.my_service.common.ICommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

/*
 * this class is supposed to declare the metadata of the OData service
 * it is invoked by the Olingo framework e.g. when the metadata document of the service is invoked
 * e.g. http://localhost:8080/ExampleService1/ExampleService1.svc/$metadata
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EdmProvider extends CsdlAbstractEdmProvider {
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(Contains.NAME_SPACE, Contains.CONTAINER_NAME);


    private final CommonProcessor commonProcessor;


    private final ApplicationContext applicationContext;

    private static List<CsdlSchema> schemaList;

    @Override
    public List<CsdlSchema> getSchemas() {
        try {
            if (Objects.nonNull(schemaList)) {
                return schemaList;
            }

            synchronized (this) {
                if (Objects.isNull(schemaList)) {
                    // create Schema
                    CsdlSchema schema = new CsdlSchema();
                    schema.setNamespace(Contains.NAME_SPACE);
                    CsdlEntityContainer entityContainer = new CsdlEntityContainer();
                    entityContainer.setName(Contains.NAME_SPACE);

                    List<CsdlEntityType> entityTypes = new ArrayList<>();
                    List<CsdlAction> actions = new ArrayList<>();
                    List<CsdlFunction> functions = new ArrayList<>();

                    Map<String, ICommonService<?>> serviceMap = commonProcessor.getService();
                    for (Map.Entry<String, ICommonService<?>> stringICommonServiceEntry : serviceMap.entrySet()) {
                        String entityName = stringICommonServiceEntry.getKey();
                        // add EntityTypes
                        entityTypes.add(getEntityType(new FullQualifiedName(Contains.NAME_SPACE, entityName)));
                        // add actions
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
                    log.info("EdmProvider.getSchemas return is : {}", JSON.toJSONString(schema));
                    schemaList = Collections.singletonList(schema);
                    return schemaList;
                }
                return schemaList;
            }
        } catch (Exception e) {
            log.error("InitEdmProvider.getSchemas has error  msg : {}", JSON.toJSONString(e));
        }
        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {
        try {
            CsdlEntityContainer entityContainer = new CsdlEntityContainer();

            List<CsdlEntitySet> entitySets = new ArrayList<>();

            List<CsdlFunctionImport> functionImports = new ArrayList<>();
            List<CsdlActionImport> actionImports = new ArrayList<>();


            Map<String, ICommonService<?>> serviceMap = commonProcessor.getService();
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
            log.info("EdmProvider.getEntityContainer return is : {}", JSON.toJSONString(entityContainer));
            return entityContainer;
        } catch (Exception e) {
            log.error("InitEdmProvider.getEntityContainer has error : ", e);
        }
        return null;
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        log.info("EdmProvider.getEntitySet param entityTypeName is : {}", JSON.toJSONString(entityTypeName));
        CsdlEntityType entityType = commonProcessor.getService().get(entityTypeName.getName()).getEntityType(entityTypeName);
        log.info("EdmProvider.getEntitySet return is : {}", JSON.toJSONString(entityType));
        return entityType;


    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
        log.info("EdmProvider.getEntitySet param entityContainer is : {} , entitySetName is : {}", JSON.toJSONString(entityContainer), entitySetName);
        CsdlEntitySet csdlEntitySet;
        csdlEntitySet = commonProcessor.getService().get(entitySetName).getEntitySet(entityContainer, entitySetName);
        log.info("EdmProvider.getEntitySet return is : {}", JSON.toJSONString(csdlEntitySet));
        return csdlEntitySet;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {
        log.info("EdmProvider.getEntityContainerInfo param entityContainerName is : {}", JSON.toJSONString(entityContainerName));
        CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
        entityContainerInfo.setContainerName(CONTAINER);
        log.info("EdmProvider.getEntityContainerInfo return is : {}", JSON.toJSONString(entityContainerInfo));
        return entityContainerInfo;
    }

    @Override
    public List<CsdlAction> getActions(FullQualifiedName actionName) {
        log.info("EdmProvider.getActions param is : {}", JSON.toJSONString(actionName));
        List<CsdlAction> csdlActionList = null;
        try {
            if (!actionName.equals(CONTAINER)) {
                csdlActionList = commonProcessor.getService().get(actionName.getName()).getActions(actionName);
            }
            log.info("EdmProvider.getActions return is : {}", JSON.toJSONString(csdlActionList));
        } catch (Exception e) {
            log.error("EdmProvider.getActions has error : {}", JSON.toJSONString(e));
        }
        return csdlActionList;
    }

    @Override
    public List<CsdlFunction> getFunctions(final FullQualifiedName functionName) {
        log.info("EdmProvider.getFunctions param is : {}", JSON.toJSONString(functionName));
        List<CsdlFunction> csdlFunctionList = null;
        try {
            if (!functionName.equals(CONTAINER)) {
                csdlFunctionList = commonProcessor.getService().get(functionName.getName()).getFunctions(functionName);
            }
            log.info("EdmProvider.getFunctions return is : {}", JSON.toJSONString(csdlFunctionList));
        } catch (Exception e) {
            log.error("EdmProvider.getFunctions has error : {}", JSON.toJSONString(e));
        }

        return csdlFunctionList;
    }

    //TODO 会进入 Container
    @Override
    public CsdlActionImport getActionImport(FullQualifiedName entityContainer, String actionImportName) {
        log.info("EdmProvider.getActionImport param entityContainer is : {} , actionImportName is : {}", JSON.toJSONString(entityContainer), actionImportName);

        CsdlActionImport csdlActionImport = null;
        try {
            if (!entityContainer.equals(CONTAINER)) {
                csdlActionImport = commonProcessor.getService().get(entityContainer.getName()).getActionImport(entityContainer, actionImportName);
            }
            log.info("EdmProvider.getActionImport return is : {}", JSON.toJSONString(csdlActionImport));
        } catch (Exception e) {
            log.error("EdmProvider.getActionImport has error : {}", JSON.toJSONString(e));
        }
        return csdlActionImport;
    }

    //TODO 会进入 Container
    @Override
    public CsdlFunctionImport getFunctionImport(final FullQualifiedName entityContainer, String functionImportName) {
        log.info("EdmProvider.getFunctionImport param entityContainer is : {} , functionImportName is : {}", JSON.toJSONString(entityContainer), functionImportName);
        CsdlFunctionImport csdlFunctionList = null;
        try {
            if (!entityContainer.equals(CONTAINER)) {
                csdlFunctionList = commonProcessor.getService().get(entityContainer.getName()).getFunctionImport(entityContainer, functionImportName);
            }
        } catch (Exception e) {
            log.error("EdmProvider.getFunctionImport has error : {}", JSON.toJSONString(e));
        }
        log.info("EdmProvider.getFunctionImport return is : {}", JSON.toJSONString(csdlFunctionList));
        return csdlFunctionList;
    }

}
