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


import com.example.demo.option.common.CommonOption;
import com.example.demo.util.Util;
import lombok.RequiredArgsConstructor;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ListEntityCollectionProcessor extends CommonEntityProcessor implements EntityCollectionProcessor {

    private OData odata;

    private ServiceMetadata serviceMetadata;


    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, SerializerException {

        //----------------------------------------自定义----------------------------------------------
        EdmEntitySet responseEdmEntitySet = null; // we'll need this to build the ContextURL
        List<UriResource> resourceParts = uriInfo.getUriResourceParts();
        int segmentCount = resourceParts.size();
        UriResource uriResource = resourceParts.get(0);
        if (!(uriResource instanceof UriResourceEntitySet)) {
            throw new ODataApplicationException("Only EntitySet is supported",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;
        EdmEntitySet startEdmEntitySet = uriResourceEntitySet.getEntitySet();
        Map<String, Object> query = new HashMap<>();
        String tableName = "";
        if (segmentCount == 1) {
            responseEdmEntitySet = startEdmEntitySet;
            tableName = responseEdmEntitySet.getName();
        } else if (segmentCount == 2) {
            UriResource lastSegment = resourceParts.get(1);
            if (lastSegment instanceof UriResourceNavigation) {
                UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) lastSegment;
                EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
                responseEdmEntitySet = Util.getNavigationTargetEntitySet(startEdmEntitySet, edmNavigationProperty);
                List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
                tableName = lastSegment.getSegmentValue();
                List<Map<String, String>> map = new ArrayList<>();
                Map<String, String> join = new HashMap<>();
                join.put("tableName", startEdmEntitySet.getName().toLowerCase());
                join.put("field", keyPredicates.get(0).getName());
                join.put("value", keyPredicates.get(0).getText());
                map.add(join);
                query.put("join", map);
            }
        } else {
            throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }

        ContextURL contextUrl = ContextURL.with().entitySet(responseEdmEntitySet).build();
        final String id = request.getRawBaseUri() + "/" + responseEdmEntitySet.getName();
        EntityCollectionSerializerOptions.Builder builder = EntityCollectionSerializerOptions.with()
                .id(id).contextURL(contextUrl);

        Map<String, CommonOption> options = applicationContext.getBeansOfType(CommonOption.class);
        for (CommonOption value : options.values()) {
            value.filter(builder, uriInfo, query);
        }

        List<?> sqlResult = getService(tableName).selectByCondition(query);

        EntityCollection entityCollection = getEntityCollection(sqlResult);
        CountOption countOption = uriInfo.getCountOption();
        if (countOption != null && countOption.getValue()) {
            entityCollection.setCount(sqlResult.size());
        }
        //-------------------------------------------------------------------------------------------

        ODataSerializer serializer = odata.createSerializer(responseFormat);

        // and serialize the content: transform from the EntitySet object to InputStream
        EdmEntityType edmEntityType = responseEdmEntitySet.getEntityType();

        EntityCollectionSerializerOptions opts = builder.build();
        SerializerResult serializedContent = serializer.entityCollection(serviceMetadata, edmEntityType, entityCollection, opts);

        // Finally: configure the response object: set the body, headers and status code
        response.setContent(serializedContent.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }
}
