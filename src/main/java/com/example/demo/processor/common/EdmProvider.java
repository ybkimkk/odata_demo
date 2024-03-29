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
import com.example.demo.contains.Contains;
import com.example.demo.methods.functionns.IFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    private static List<CsdlSchema> SCHEMA_LIST;
    private static CsdlEntityContainer ENTITY_CONTAINER;
    private static CsdlEntityContainerInfo ENTITY_CONTAINER_INFO;

    @Override
    public List<CsdlSchema> getSchemas() {
        try {
            if (Objects.nonNull(SCHEMA_LIST)) {
                return SCHEMA_LIST;
            }

            synchronized (this) {
                if (Objects.isNull(SCHEMA_LIST)) {
                    // create Schema
                    CsdlSchema schema = new CsdlSchema();
                    schema.setNamespace(Contains.NAME_SPACE);
                    CsdlEntityContainer entityContainer = new CsdlEntityContainer();
                    entityContainer.setName(Contains.NAME_SPACE);

                    // create EntitySets
                    List<CsdlFunction> functions = new ArrayList<>();
                    // add EntitySets
                    schema.setEntityTypes(commonProcessor.getEntityType(null));
                    // add Actions
                    schema.setActions(commonProcessor.getAction(null));
                    // add EntityContainer
                    schema.setEntityContainer(getEntityContainer());
                    log.info("EdmProvider.getSchemas return is : {}", JSON.toJSONString(schema));
                    SCHEMA_LIST = Collections.singletonList(schema);
                    return SCHEMA_LIST;
                }
                return SCHEMA_LIST;
            }
        } catch (Exception e) {
            log.error("InitEdmProvider.getSchemas has error msg : {}", JSON.toJSONString(e));
        }
        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {
        try {
            if (Objects.nonNull(ENTITY_CONTAINER)) {
                return ENTITY_CONTAINER;
            }

            synchronized (this) {
                if (Objects.isNull(ENTITY_CONTAINER)) {
                    List<CsdlEntitySet> entitySets = commonProcessor.getEntitySet(null);
                    List<CsdlActionImport> actionImport = commonProcessor.getActionImport(null);
                    CsdlEntityContainer container = new CsdlEntityContainer();
                    container.setName(Contains.CONTAINER_NAME);
                    container.setActionImports(actionImport);
                    container.setEntitySets(entitySets);
                    ENTITY_CONTAINER = container;
                    return ENTITY_CONTAINER;
                }
                return ENTITY_CONTAINER;
            }
        } catch (Exception e) {
            log.error("InitEdmProvider.getEntityContainer has error : ", e);
        }

        return null;
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        log.info("EdmProvider.getEntityType param entityTypeName is : {}", JSON.toJSONString(entityTypeName));
        CsdlEntityType entityType = null;
        List<CsdlEntityType> entityTypes = commonProcessor.getEntityType(entityTypeName.getName());
        if (CollUtil.isNotEmpty(entityTypes)) {
            entityType = entityTypes.get(0);
        }
        log.info("EdmProvider.getEntityType return is : {}", JSON.toJSONString(entityType));
        return entityType;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
        log.info("EdmProvider.getEntitySet param entityContainer is : {} , entitySetName is : {}", JSON.toJSONString(entityContainer), entitySetName);
        CsdlEntitySet csdlEntitySet = null;
        List<CsdlEntitySet> csdlEntitySets = commonProcessor.getEntitySet(entitySetName);
        if (CollUtil.isNotEmpty(csdlEntitySets)) {
            csdlEntitySet = csdlEntitySets.get(0);
        }
        log.info("EdmProvider.getEntitySet return is : {}", JSON.toJSONString(csdlEntitySet));
        return csdlEntitySet;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {
        log.info("EdmProvider.getEntityContainerInfo param entityContainerName is : {}", JSON.toJSONString(entityContainerName));
        if (Objects.nonNull(ENTITY_CONTAINER_INFO)) {
            return ENTITY_CONTAINER_INFO;

        }

        synchronized (this) {
            if (Objects.isNull(ENTITY_CONTAINER_INFO)){
                CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
                entityContainerInfo.setContainerName(CONTAINER);
                ENTITY_CONTAINER_INFO = entityContainerInfo;
                return ENTITY_CONTAINER_INFO;
            }
            return ENTITY_CONTAINER_INFO;
        }
    }

    @Override
    public List<CsdlAction> getActions(FullQualifiedName actionName) {
        log.info("EdmProvider.getActions param is : {}", JSON.toJSONString(actionName));
        List<CsdlAction> csdlActionList = commonProcessor.getAction(actionName.getName());
        log.info("EdmProvider.getActions return is : {}", JSON.toJSONString(csdlActionList));
        return csdlActionList;
    }

    @Override
    public CsdlActionImport getActionImport(FullQualifiedName entityContainer, String actionImportName) {
        log.info("EdmProvider.getActionImport param entityContainer is : {} , actionImportName is : {}", JSON.toJSONString(entityContainer), actionImportName);
        List<CsdlActionImport> csdlActionImport = commonProcessor.getActionImport(actionImportName);
        log.info("EdmProvider.getActionImport return is : {}", JSON.toJSONString(csdlActionImport));
        return csdlActionImport.get(0);

    }


    @Override
    public List<CsdlFunction> getFunctions(final FullQualifiedName functionName) {
        log.info("EdmProvider.getFunctions param is : {}", JSON.toJSONString(functionName));
        List<CsdlFunction> csdlFunctionList = null;
        IFunction function = commonProcessor.getFunction(functionName.getName());
        if (Objects.nonNull(function)) {
            csdlFunctionList = function.getFunctions();
        }
        log.info("EdmProvider.getFunctions return is : {}", JSON.toJSONString(csdlFunctionList));

        return csdlFunctionList;
    }

    //TODO 会进入 Container
    @Override
    public CsdlFunctionImport getFunctionImport(final FullQualifiedName entityContainer, String functionImportName) {
        log.info("EdmProvider.getFunctionImport param entityContainer is : {} , functionImportName is : {}", JSON.toJSONString(entityContainer), functionImportName);
        CsdlFunctionImport csdlFunctionList = null;
        IFunction function = commonProcessor.getFunction(entityContainer.getName());
        if (Objects.nonNull(function)) {
            csdlFunctionList = function.getFunctionImport();
        }
        log.info("EdmProvider.getFunctionImport return is : {}", JSON.toJSONString(csdlFunctionList));
        return csdlFunctionList;
    }

}
