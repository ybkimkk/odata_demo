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

import com.alibaba.fastjson2.JSON;
import com.example.demo.contains.Contains;
import com.example.demo.entity.common.IEntity;
import com.example.demo.methods.functionns.IFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.springframework.stereotype.Component;

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
                    List<CsdlFunction> functions = new ArrayList<>();

                    Map<String, IEntity> entityMap = commonProcessor.getEntity();
                    for (Map.Entry<String, IEntity> entityEntry : entityMap.entrySet()) {
                        entityTypes.add(commonProcessor.getEntity(entityEntry.getKey()).getEntityType());
                    }
                    schema.setEntityTypes(entityTypes);

                    List<CsdlAction> action = commonProcessor.getAction();
                    schema.setActions(action);

                    // add EntityContainer
                    schema.setEntityContainer(getEntityContainer());
                    log.info("EdmProvider.getSchemas return is : {}", JSON.toJSONString(schema));
                    schemaList = Collections.singletonList(schema);
                    return schemaList;
                }
                return schemaList;
            }
        } catch (Exception e) {
            log.error("InitEdmProvider.getSchemas has error msg : {}", JSON.toJSONString(e));
        }
        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {
        try {

            List<CsdlEntitySet> entitySets = new ArrayList<>();

            Map<String, IEntity> entity = commonProcessor.getEntity();
            for (Map.Entry<String, IEntity> entityEntry : entity.entrySet()) {
                entitySets.add(commonProcessor.getEntity(entityEntry.getKey()).getEntitySet());
            }

            List<CsdlActionImport> actionImport = commonProcessor.getActionImports();

            CsdlEntityContainer entityContainer = new CsdlEntityContainer();
            entityContainer.setName(Contains.CONTAINER_NAME);
            entityContainer.setActionImports(actionImport);
            entityContainer.setEntitySets(entitySets);
            log.info("EdmProvider.getEntityContainer return is : {}", JSON.toJSONString(entityContainer));
            return entityContainer;
        } catch (Exception e) {
            log.error("InitEdmProvider.getEntityContainer has error : ", e);
        }
        return null;
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        log.info("EdmProvider.getEntityType param entityTypeName is : {}", JSON.toJSONString(entityTypeName));
        CsdlEntityType entityType = commonProcessor.getEntity(entityTypeName.getName()).getEntityType();
        log.info("EdmProvider.getEntityType return is : {}", JSON.toJSONString(entityType));
        return entityType;


    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
        log.info("EdmProvider.getEntitySet param entityContainer is : {} , entitySetName is : {}", JSON.toJSONString(entityContainer), entitySetName);
        CsdlEntitySet csdlEntitySet = null;
        IEntity entity = commonProcessor.getEntity(entitySetName);
        if (Objects.nonNull(entity)) {
            csdlEntitySet = entity.getEntitySet();
        }
        log.info("EdmProvider.getEntitySet return is : {}", JSON.toJSONString(csdlEntitySet));
        return csdlEntitySet;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {
        log.info("EdmProvider.getEntityContainerInfo param entityContainerName is : {}", JSON.toJSONString(entityContainerName));
        CsdlEntityContainerInfo entityContainerInfo = null;
        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
            entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER);
        }
        log.info("EdmProvider.getEntityContainerInfo return is : {}", JSON.toJSONString(entityContainerInfo));
        return entityContainerInfo;
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
