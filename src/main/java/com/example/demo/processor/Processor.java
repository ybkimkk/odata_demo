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


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.example.demo.entity.common.CommonEntity;
import com.example.demo.entity.odata.OdataRequestEntity;
import com.example.demo.option.common.CommonOption;
import com.example.demo.processor.common.CommonProcessor;
import com.example.demo.util.OdataUtil;
import com.example.demo.util.Util;
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
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Processor extends CommonProcessor implements org.apache.olingo.server.api.processor.EntityProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;


    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }


    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws SerializerException, ODataApplicationException {

        // 1. retrieve the Entity Type
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        List<OdataRequestEntity> edmHelper = getEdmHelper(uriInfo.getUriResourceParts());

        EdmEntitySet edmEntitySet = null;
        EdmEntityType entityType = null;
        //说明只有一个路径
        if (edmHelper.size() == 1) {
            edmEntitySet = edmHelper.get(0).getEdmEntitySet();
            entityType = edmHelper.get(0).getEntityType();
        } else if (edmHelper.size() > 1) {
            OdataRequestEntity odataRequestEntity = edmHelper.stream().reduce((x, y) -> y).orElseThrow(() -> new ODataApplicationException("No Entity Type found.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH));
            edmEntitySet = odataRequestEntity.getEdmEntitySet();
            entityType = odataRequestEntity.getEntityType();
//TODO 多路径 有说法
        } else {
            throw new ODataApplicationException("No Entity Type found.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }


//        //如果为深度路径则 查找最后一个路径的set 如果/Test(1)/TestItem  则查找最后一个TestItem
//        if (resourcePaths.get(resourcePaths.size() - 1) instanceof UriResourceNavigation) {
//            UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) resourcePaths.get(resourcePaths.size() - 1);
//            EdmNavigationProperty property = uriResourceNavigation.getProperty();
//            entityType = property.getType();
//            edmEntitySet = (EdmEntitySet) ((UriResourceEntitySet) resourcePaths.get(0))
//                    .getEntitySet()
//                    .getRelatedBindingTarget(property.getName());
//        } else {
//            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
//            edmEntitySet = uriResourceEntitySet.getEntitySet();
//            entityType = edmEntitySet.getEntityType();
//        }

        ContextURL contextUrl = ContextURL.with()
                .entitySet(edmEntitySet)
                .build();

        EntitySerializerOptions.Builder builder = EntitySerializerOptions.with()
                .contextURL(contextUrl);

        Map<String, CommonOption> options = applicationContext.getBeansOfType(CommonOption.class);

        Map<String, Object> query = new HashMap<>();
        for (CommonOption value : options.values()) {
            value.filter(uriInfo, query);
        }

        List<Map<String, String>> mapList = new ArrayList<>();
        for (OdataRequestEntity odataRequestEntity : edmHelper) {
            if (Objects.nonNull(odataRequestEntity.getJoin())) {
                List<CommonEntity.Join> join = odataRequestEntity.getJoin();
                mapList = join.stream().map(x -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("field", x.getField());
                    map.put("table", StrUtil.toUnderlineCase(x.getTable()));
                    map.put("value", x.getValue());
                    return map;
                }).collect(Collectors.toList());

            }
        }
        query.put("join",mapList);


        List<?> list = getService(edmEntitySet.getName()).selectByCondition(query);
        if (CollUtil.isEmpty(list)) {
            log.info("No requested resource msg:{}", JSON.toJSONString(query));
            throw new ODataApplicationException("No requested resource", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        }

        EntityCollection entityCollection = OdataUtil.getEntityCollection(list);
        //--------------------------------------------------------------------------------------------------------------

        // 3. serialize
        EntitySerializerOptions opts = builder
                .expand(uriInfo.getExpandOption())
                .build();

        ODataSerializer serializer = this.odata.createSerializer(responseFormat);

        SerializerResult result = serializer.entity(serviceMetadata, entityType, entityCollection.getEntities().stream().findFirst().orElse(new Entity()), opts);

        //4. configure the response object
        response.setContent(result.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }


    private List<OdataRequestEntity> getEdmHelper(List<UriResource> resourcePaths) {
        List<OdataRequestEntity> odataRequestEntities = new ArrayList<>();
        List<CommonEntity.Join> joins = new ArrayList<>();

        for (UriResource resourcePath : resourcePaths) {
            OdataRequestEntity odataRequestEntity = new OdataRequestEntity();
            if (resourcePath instanceof UriResourceNavigation) {
                UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) resourcePath;
                EdmNavigationProperty property = uriResourceNavigation.getProperty();
                odataRequestEntity.setEntityType(property.getType());
                UriResourceEntitySet path = (UriResourceEntitySet) resourcePaths.get(0);
                EdmEntitySet edmEntitySet = path.getEntitySet();
                EdmEntitySet entitySet = (EdmEntitySet) edmEntitySet.getRelatedBindingTarget(property.getName());
                odataRequestEntity.setEdmEntitySet(entitySet);
            } else {
                UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePath;
                EdmEntitySet entitySet = uriResourceEntitySet.getEntitySet();

                for (UriParameter keyPredicate : uriResourceEntitySet.getKeyPredicates()) {
                    CommonEntity.Join join = new CommonEntity.Join();
                    join.setTable(entitySet.getName());
                    join.setValue(keyPredicate.getText());
                    join.setField(keyPredicate.getName());
                    joins.add(join);
                }
                odataRequestEntity.setEntityType(entitySet.getEntityType());
                odataRequestEntity.setEdmEntitySet(entitySet);
                odataRequestEntity.setJoin(joins);
            }
            odataRequestEntities.add(odataRequestEntity);
        }
        return odataRequestEntities;
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
        Object insert = getService(edmEntitySet.getName()).insert(OdataUtil.convertEntityToMap(result.getEntity()));
        EntityCollection entityCollection = OdataUtil.getEntityCollection(Collections.singletonList(insert));
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
        Map<String, Object> mapByEntity = OdataUtil.convertEntityToMap(result.getEntity());
        mapByEntity.put("ID", uriParameter.getText());
        Object insert = getService(edmEntitySet.getName()).update(mapByEntity);
        EntityCollection entityCollection = OdataUtil.getEntityCollection(Collections.singletonList(insert));
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
        response.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
