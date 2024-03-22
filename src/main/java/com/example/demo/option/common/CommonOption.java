package com.example.demo.option.common;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.uri.UriInfo;

import java.util.Map;

public interface CommonOption {
    void filter(EntityCollectionSerializerOptions.Builder builder, UriInfo uriInfo, Map<String, Object> query) throws ODataApplicationException;

}
