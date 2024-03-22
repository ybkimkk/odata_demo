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
package com.example.demo.processor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.example.demo.contains.Contains;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.ex.ODataException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * this class is supposed to declare the metadata of the OData service
 * it is invoked by the Olingo framework e.g. when the metadata document of the service is invoked
 * e.g. http://localhost:8080/ExampleService1/ExampleService1.svc/$metadata
 */
@Component
@Slf4j
public class InitEdmProvider extends CsdlAbstractEdmProvider {
    public static List<String> ENTITY_NAME_LIST = new ArrayList<>();
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(Contains.NAME_SPACE, Contains.CONTAINER_NAME);
    @Resource
    private ApplicationContext applicationContext;


    @PostConstruct
    public void init() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory(resolver);
        String packageSearchPath = ClassUtils.convertClassNameToResourcePath(Contains.ENTITY_PACKAGE) + "/*.class";
        try {
            for (org.springframework.core.io.Resource resource : resolver.getResources(packageSearchPath)) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                String className = metadataReader.getClassMetadata().getClassName();
                ENTITY_NAME_LIST.add(className.substring(className.lastIndexOf('.') + 1).replace("Entity", StrUtil.EMPTY));
            }
        } catch (IOException e) {
            log.error("InitEdmProvider.init has error :", e);
        }
    }


    @Override
    public List<CsdlSchema> getSchemas() {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(Contains.NAME_SPACE);
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        for (String strClass : ENTITY_NAME_LIST) {
            entityTypes.add(getEntityType(new FullQualifiedName(Contains.NAME_SPACE, strClass)));
            entitySets.add(getEntitySet(CONTAINER, strClass));
        }
        entityContainer.setEntitySets(entitySets);
        schema.setEntityTypes(entityTypes);
        entityContainer.setName(Contains.CONTAINER_NAME);
        schema.setEntityContainer(entityContainer);
        return Collections.singletonList(schema);
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        try {
            Object bean = applicationContext.getBean(entityTypeName.getName() + "Service");
            Class<?> serviceClass = bean.getClass();
            Method method = serviceClass.getDeclaredMethod("getEntityType", FullQualifiedName.class);
            method.setAccessible(true);
            return (CsdlEntityType) method.invoke(bean, entityTypeName);
        } catch (NoSuchMethodException e) {
            log.error("InitEdmProvider.getEntityType Method not found --> msg: {}, param :{}", e, entityTypeName.getName());
        } catch (IllegalAccessException e) {
            log.error("InitEdmProvider.getEntityType Illegal access --> msg: {}, param :{}", e, entityTypeName.getName());
        } catch (InvocationTargetException e) {
            log.error("InitEdmProvider.getEntityType Invocation target exception --> msg: {}, param :{}", e, entityTypeName.getName());
        }
        return null;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {
        try {
            Object bean = applicationContext.getBean(entitySetName + "Service");
            Class<?> serviceClass = bean.getClass();
            Method method = serviceClass.getDeclaredMethod("getEntitySet", FullQualifiedName.class, String.class);
            method.setAccessible(true);
            return (CsdlEntitySet) method.invoke(bean, entityContainer, entitySetName);
        } catch (NoSuchMethodException e) {
            log.error("InitEdmProvider.getEntitySet Method not found --> msg : {},param : {}", e, JSON.toJSONString(entitySetName));
        } catch (IllegalAccessException e) {
            log.error("InitEdmProvider.getEntitySet Illegal access --> msg: {},param : {}", e, JSON.toJSONString(entitySetName));
        } catch (InvocationTargetException e) {
            log.error("InitEdmProvider.getEntitySet Invocation target exception --> msg: {},param : {}", e, JSON.toJSONString(entitySetName));
        }
        return null;

    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {
        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER);
            return entityContainerInfo;
        }
        return null;
    }


    @Override
    public List<CsdlAction> getActions(FullQualifiedName actionName) throws ODataException {
        return super.getActions(actionName);
    }

    @Override
    public CsdlActionImport getActionImport(FullQualifiedName entityContainer, String actionImportName) throws ODataException {
        return super.getActionImport(entityContainer, actionImportName);
    }

    @Override
    public List<CsdlFunction> getFunctions(FullQualifiedName functionName) throws ODataException {
        return super.getFunctions(functionName);
    }

    @Override
    public CsdlFunctionImport getFunctionImport(FullQualifiedName entityContainer, String functionImportName) throws ODataException {
        return super.getFunctionImport(entityContainer, functionImportName);
    }
}
