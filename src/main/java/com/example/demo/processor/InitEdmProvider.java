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

import com.example.demo.service.TestService;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 * this class is supposed to declare the metadata of the OData service
 * it is invoked by the Olingo framework e.g. when the metadata document of the service is invoked
 * e.g. http://localhost:8080/ExampleService1/ExampleService1.svc/$metadata
 */
@Component
public class InitEdmProvider extends CsdlAbstractEdmProvider {

    // Service Namespace

    public static String NAMESPACE = "OData.Demo";

    // EDM Container

    public static String CONTAINER_NAME = "Container";
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);


    @Autowired
    private TestService testService;


    @Override
    public List<CsdlSchema> getSchemas() {

        // create Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        // add EntityTypes
//        List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
//        entityTypes.add(getEntityType(ET_PRODUCT_FQN));
//        schema.setEntityTypes(entityTypes);

        // add EntityContainer
        schema.setEntityContainer(getEntityContainer());

        // finally
        List<CsdlSchema> schemas = new ArrayList<>();
        schemas.add(schema);

        return schemas;
    }


    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        //create EntityType properties
        CsdlProperty id = new CsdlProperty().setName("ID")
                .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        CsdlProperty name = new CsdlProperty().setName("NAME")
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        CsdlProperty age = new CsdlProperty().setName("AGE")
                .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        CsdlProperty price = new CsdlProperty().setName("PRICE")
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
        // create CsdlPropertyRef for Key element
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("ID");

        // configure EntityType
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("Test");
        entityType.setProperties(Arrays.asList(id, name, age, price));
        entityType.setKey(Collections.singletonList(propertyRef));

        return entityType;


    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {

        if (entityContainer.equals(CONTAINER)) {
            CsdlEntitySet entitySet = new CsdlEntitySet();
            entitySet.setName(entitySetName);
            entitySet.setType(getAllFullQualifiedName("Test"));
            return entitySet;
        }

        return null;

    }


    public CsdlEntityContainer getEntityContainer() {
        // create EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        //TODO home page
        entitySets.add(getEntitySet(CONTAINER, "Tests"));

        // create EntityContainer
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        entityContainer.setEntitySets(entitySets);

        return entityContainer;

    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {

        // This method is invoked when displaying the service document at
        // e.g. http://localhost:8080/DemoService/DemoService.svc
        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER);
            return entityContainerInfo;
        }
        return null;
    }

    private FullQualifiedName getAllFullQualifiedName(String name) {
        return new FullQualifiedName(NAMESPACE, name);
//        new FullQualifiedName(NAMESPACE, ET_PRODUCT_NAME);
    }
}
