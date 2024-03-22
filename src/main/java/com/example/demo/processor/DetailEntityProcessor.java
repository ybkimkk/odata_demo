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


import com.example.demo.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DetailEntityProcessor extends CommonEntityProcessor implements EntityProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Autowired
    InitEdmProvider initEdmProvider;


    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }


    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws SerializerException, ODataApplicationException {

        // 1. retrieve the Entity Type
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        // Note: only in our example we can assume that the first segment is the EntitySet

        //--------------------------------------------------------------------------------------------------------------
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType entityType = edmEntitySet.getEntityType();
        if (resourcePaths.get(resourcePaths.size() - 1) instanceof UriResourceNavigation) {
            UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) resourcePaths.get(resourcePaths.size() - 1);
            EdmNavigationProperty property = uriResourceNavigation.getProperty();
            entityType = property.getType();
            edmEntitySet = Util.getNavigationTargetEntitySet(edmEntitySet, property);
        }

        Map<String, Object> query = new HashMap<>();
        query.put(keyPredicates.get(0).getName(), Integer.valueOf(keyPredicates.get(0).getText()));
        EntityCollection entityCollection = getEntityCollection(getService(edmEntitySet.getName()).selectByCondition(query));
        //--------------------------------------------------------------------------------------------------------------


        // 3. serialize


        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).suffix(ContextURL.Suffix.ENTITY).build();
        // expand and select currently not supported
        EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

        ODataSerializer serializer = this.odata.createSerializer(responseFormat);

        SerializerResult result = serializer.entity(serviceMetadata, entityType, entityCollection.getEntities().stream().findFirst().orElse(null), options);

        //4. configure the response object
        response.setContent(result.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    /*
     * Example request:
     *
     * POST URL: http://localhost:8080/DemoService/DemoService.svc/Products
     * Header: Content-Type: application/json; odata.metadata=minimal
     * Request body:
         {
            "ID":3,
            "Name":"Ergo Screen",
            "Description":"17 Optimum Resolution 1024 x 768 @ 85Hz, resolution 1280 x 960"
        }
     * */
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,
                             ContentType requestFormat, ContentType responseFormat)
            throws ODataApplicationException, DeserializerException, SerializerException {

        // 1. Retrieve the entity type from the URI
        EdmEntitySet edmEntitySet = Util.getEdmEntitySet(uriInfo);
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // 2. create the data in backend
        // 2.1. retrieve the payload from the POST request for the entity to create and deserialize it
        InputStream requestInputStream = request.getBody();
        ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
        DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);


        //--------------------------------------------------------------------------------------------------------------
        Object insert = getService(edmEntitySet.getName()).insert(convertEntityToMap(result.getEntity()));
        EntityCollection entityCollection = getEntityCollection(Collections.singletonList(insert));
        Entity createdEntity = entityCollection.getEntities().stream().findFirst().orElse(null);
        //--------------------------------------------------------------------------------------------------------------


        // 3. serialize the response (we have to return the created entity)
        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
        EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build(); // expand and select currently not supported

        ODataSerializer serializer = this.odata.createSerializer(responseFormat);
        SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, options);

        //4. configure the response object
        final String location = request.getRawBaseUri() + '/'
                + odata.createUriHelper().buildCanonicalURL(edmEntitySet, createdEntity);

        response.setHeader(HttpHeader.LOCATION, location);
        response.setContent(serializedResponse.getContent());
        response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }


    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws DeserializerException, SerializerException, ODataApplicationException {

        // 1. Retrieve the entity set which belongs to the requested entity
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        // Note: only in our example we can assume that the first segment is the EntitySet
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // 2. update the data in backend
        // 2.1. retrieve the payload from the PUT request for the entity to be updated
        InputStream requestInputStream = request.getBody();
        ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
        DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);

        //--------------------------------------------------------------------------------------------------------------
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        UriParameter uriParameter = keyPredicates.stream().findFirst().orElse(null);
        Map<String, Object> mapByEntity = convertEntityToMap(result.getEntity());
        mapByEntity.put("ID", uriParameter.getText());
        Object insert = getService(edmEntitySet.getName()).update(mapByEntity);
        EntityCollection entityCollection = getEntityCollection(Collections.singletonList(insert));
        Entity requestEntity = entityCollection.getEntities()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ODataApplicationException("DetailEntityProcessor has error", 500, Locale.ROOT));


        ODataSerializer serializer = this.odata.createSerializer(responseFormat);
        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
        EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build(); // expand and select currently not supported
        SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, requestEntity, options);
        //4. configure the response object
        final String location = request.getRawBaseUri() + '/' + odata.createUriHelper().buildCanonicalURL(edmEntitySet, requestEntity);

        response.setHeader(HttpHeader.LOCATION, location);
        response.setContent(serializedResponse.getContent());
        response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        //--------------------------------------------------------------------------------------------------------------
    }


    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
            throws ODataApplicationException {

        // 1. Retrieve the entity set which belongs to the requested entity
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        // Note: only in our example we can assume that the first segment is the EntitySet
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        // 2. delete the data in backend
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();

        //--------------------------------------------------------------------------------------------------------------
        String id = keyPredicates.stream().map(UriParameter::getText).findFirst().orElseThrow(RuntimeException::new);
        getService(edmEntitySet.getName()).delete(id);
        //--------------------------------------------------------------------------------------------------------------

        //3. configure the response object
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    }
}
