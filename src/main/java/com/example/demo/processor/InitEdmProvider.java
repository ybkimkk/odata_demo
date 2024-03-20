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

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/*
 * this class is supposed to declare the metadata of the OData service
 * it is invoked by the Olingo framework e.g. when the metadata document of the service is invoked
 * e.g. http://localhost:8080/ExampleService1/ExampleService1.svc/$metadata
 */
@Component
@Slf4j
public class InitEdmProvider extends CsdlAbstractEdmProvider {

    // Service Namespace

    public static String NAMESPACE = "OData.Demo";

    // EDM Container

    public static String CONTAINER_NAME = "Container";
    public final static String ENTITY_PACKAGE = "com.example.demo.entity";
    public static List<String> ENTITY_NAME_LIST = new ArrayList<>();
    public static List<Map<String, Field[]>> ENTITY_BEAN_LIST = new ArrayList<>();
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);


    @PostConstruct
    public void init() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory(resolver);
        String packageSearchPath = ClassUtils.convertClassNameToResourcePath(ENTITY_PACKAGE) + "/*.class";
        try {
            for (org.springframework.core.io.Resource resource : resolver.getResources(packageSearchPath)) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                String className = metadataReader.getClassMetadata().getClassName();
                ENTITY_NAME_LIST.add(className.substring(className.lastIndexOf('.') + 1).replace("Entity", StrUtil.EMPTY));
                Class<?> clazz = Class.forName(className);
                Field[] declaredFields = clazz.getDeclaredFields();
                Map<String, Field[]> stringHashMap = new HashMap<>();
                stringHashMap.put(className, declaredFields);
                ENTITY_BEAN_LIST.add(stringHashMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("getSchemas error :{}", e.toString());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<CsdlSchema> getSchemas() {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        List<CsdlEntityType> entityTypes = new ArrayList<>();
        for (String strClass : ENTITY_NAME_LIST) {
            entityTypes.add(getEntityType(new FullQualifiedName(NAMESPACE, strClass)));
        }

        schema.setEntityTypes(entityTypes);
        schema.setEntityContainer(getEntityContainer());

        // finally
        List<CsdlSchema> schemas = new ArrayList<>();
        schemas.add(schema);

        return schemas;
    }


    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        CsdlEntityType entityType = new CsdlEntityType();
        try {
            String entityName = ENTITY_PACKAGE + "." + entityTypeName.getName() + "Entity";
            Field[] declaredFields = ENTITY_BEAN_LIST
                    .stream()
                    .filter(x -> Objects.nonNull(x.get(entityName)))
                    .map(x -> x.get(entityName))
                    .findFirst().orElseThrow(RuntimeException::new);

            ArrayList<CsdlProperty> csdlPropertyArrayList = new ArrayList<>();
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();

            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(TableId.class)) {
                    propertyRef.setName(field.getName().toUpperCase());
                }
                field.setAccessible(true);
                Class<?> type = field.getType();
                csdlPropertyArrayList.add(
                        new CsdlProperty()
                                .setName(field.getName().toUpperCase())
                                .setType(getFullQualifiedName(type.getName()))
                );
            }

            entityType.setName(entityTypeName.getName());
            entityType.setProperties(csdlPropertyArrayList);
            entityType.setKey(Collections.singletonList(propertyRef));
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }

        return entityType;


    }

    private FullQualifiedName getFullQualifiedName(String fieldType) {
        switch (fieldType) {
            case "int":
            case "java.lang.Integer":
                return EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
            case "long":
            case "java.lang.Long":
                return EdmPrimitiveTypeKind.Int64.getFullQualifiedName();
            case "boolean":
            case "java.lang.Boolean":
                return EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
            case "java.lang.String":
            default:
                return EdmPrimitiveTypeKind.String.getFullQualifiedName();

        }
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {

        if (entityContainer.equals(CONTAINER)) {
            CsdlEntitySet entitySet = new CsdlEntitySet();
            entitySet.setName(entitySetName);
            entitySet.setType(getAllFullQualifiedName(entitySetName));
            return entitySet;
        }

        return null;

    }


    public CsdlEntityContainer getEntityContainer() {
        // create EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        for (String s : ENTITY_NAME_LIST) {
            entitySets.add(getEntitySet(CONTAINER, s));
        }

        // create EntityContainer
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        entityContainer.setEntitySets(entitySets);

        return entityContainer;

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

    private FullQualifiedName getAllFullQualifiedName(String name) {
        return new FullQualifiedName(NAMESPACE, name);
    }
}
