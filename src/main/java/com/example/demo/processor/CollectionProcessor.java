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
import com.example.demo.option.common.CommonOption;
import com.example.demo.processor.common.CommonProcessor;
import com.example.demo.util.OdataUtil;
import com.example.demo.util.Util;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.*;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class CollectionProcessor implements org.apache.olingo.server.api.processor.EntityCollectionProcessor {

    private OData odata;

    private ServiceMetadata serviceMetadata;

    @Resource
    private CommonProcessor commonProcessor;

    @Resource
    private ApplicationContext applicationContext;


    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @SneakyThrows
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, SerializerException {

        //----------------------------------------自定义----------------------------------------------
        List<UriResource> resourceParts = uriInfo.getUriResourceParts();
        UriResource uriResource = resourceParts.stream().reduce((x, y) -> y).orElse(null);
        if (Objects.isNull(uriResource)) {
            throw new ODataApplicationException("Bad request", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }
        //mybatis查询对象
        Map<String, Object> query = new HashMap<>();
        //获取表名
        String tableName = uriResource.getSegmentValue();

        EdmEntitySet edmEntitySet;
        //单个路径
        if (uriResource instanceof UriResourceEntitySet) {
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;
            edmEntitySet = uriResourceEntitySet.getEntitySet();
        }
        // 深度路径时`
        else if (uriResource instanceof UriResourceNavigation) {
            UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) uriResource;
            EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.stream().findFirst().orElse(null);
            edmEntitySet = Util.getNavigationTargetEntitySet(uriResourceEntitySet.getEntitySet(), edmNavigationProperty);
            List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
            List<Map<String, String>> map = new ArrayList<>();
            for (int i = 1; i < resourceParts.size(); i++) {
                int index = i - 1;
                Map<String, String> join = new HashMap<>();
                join.put("tableName", StrUtil.toUnderlineCase(resourceParts.get(index).getSegmentValue()));
                join.put("field", keyPredicates.get(index).getName());
                join.put("value", keyPredicates.get(index).getText());
                map.add(join);
                query.put("join", map);
            }
        } else {
            throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }


        ContextURL contextUrl = ContextURL.with()
                .entitySet(edmEntitySet)
                .build();
        final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
        EntityCollectionSerializerOptions.Builder builder = EntityCollectionSerializerOptions.with().id(id).contextURL(contextUrl);

        Map<String, CommonOption> options = applicationContext.getBeansOfType(CommonOption.class);
        for (CommonOption value : options.values()) {
            value.filter(uriInfo, query);
        }

        //查询数据库
        List<?> sqlResult = commonProcessor.getService(tableName).selectByCondition(query);
        if (CollUtil.isEmpty(sqlResult)) {
            log.info("No requested resource msg:{}", JSON.toJSONString(query));
            throw new ODataApplicationException("No requested resource", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        }
        //组装成odata对象
        EntityCollection entityCollection = OdataUtil.getEntityCollection(sqlResult);
        //获取expand判断值
        ExpandOption expandOption = uriInfo.getExpandOption();

        //说明有expand组装对象
        if (query.containsKey("expand")) {
            List<Entity> entities = entityCollection.getEntities();
            for (Entity entity : entities) {
                Link link = new Link();
                link.setTitle(expandOption.getText());
                link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
                link.setRel(Constants.NS_ASSOCIATION_LINK_REL + expandOption.getText());
                List<Property> properties = entity.getProperties();
                for (Property property : properties) {
                    if (property.getValue() instanceof List) {
                        EntityCollection subCollection = OdataUtil.getEntityCollection((List<?>) property.getValue());
                        link.setInlineEntitySet(subCollection);
                    }
                }

                entity.getNavigationLinks().add(link);
            }
        }
        //获取count判断值
        CountOption countOption = uriInfo.getCountOption();
        if (countOption != null && countOption.getValue()) {
            entityCollection.setCount(sqlResult.size());
        }
        //-------------------------------------------------------------------------------------------

        ODataSerializer serializer = odata.createSerializer(responseFormat);
        // and serialize the content: transform from the EntitySet object to InputStream
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        EntityCollectionSerializerOptions opts = builder
                .count(countOption)
                .expand(expandOption)
                .select(uriInfo.getSelectOption())
                .build();

        SerializerResult serializedContent = serializer.entityCollection(serviceMetadata, edmEntityType, entityCollection, opts);

        // Finally: configure the response object: set the body, headers and status code
        response.setContent(serializedContent.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }
}
