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
package com.example.demo.data;


import com.example.demo.util.Util;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class Storage {

    private List<Entity> productList;
    private List<Entity> productListBeforeTransaction;

    public void beginTransaction() {
        if (productListBeforeTransaction == null) {
            productListBeforeTransaction = cloneEntityCollection(productList);
        }
    }

    public void commitTransaction() {
        if (productListBeforeTransaction != null) {
            productListBeforeTransaction = null;
        }
    }

    public void rollbackTranscation() {
        if (productListBeforeTransaction != null) {
            productList = productListBeforeTransaction;
            productListBeforeTransaction = null;
        }
    }

    /* INTERNAL */

    private List<Entity> cloneEntityCollection(final List<Entity> entities) {
        final List<Entity> clonedEntities = new ArrayList<Entity>();

        for (final Entity entity : entities) {
            final Entity clonedEntity = new Entity();

            clonedEntity.setId(entity.getId());
            for (final Property property : entity.getProperties()) {
                clonedEntity.addProperty(new Property(property.getType(),
                        property.getName(),
                        property.getValueType(),
                        property.getValue()));
            }

            clonedEntities.add(clonedEntity);
        }

        return clonedEntities;
    }

}
