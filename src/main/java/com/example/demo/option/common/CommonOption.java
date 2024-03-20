package com.example.demo.option.common;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.uri.UriInfo;

import java.util.List;

public interface CommonOption {
    List<?> filter(EntityCollectionSerializerOptions.Builder builder, UriInfo uriInfo, List<?> list) throws ODataApplicationException;

    int getOrder();
}
