package com.example.demo.option.common;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.uri.UriInfo;

public interface CommonOption {
    public void checkOption(EntityCollectionSerializerOptions.Builder builder, UriInfo uriInfo, EntityCollection entityCollection);
}
