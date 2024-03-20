package com.example.demo.option;

import com.example.demo.option.common.CommonOption;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.uri.UriInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SelectOption implements CommonOption {
    @Override
    public void checkOption(EntityCollectionSerializerOptions.Builder builder, UriInfo uriInfo, EntityCollection entityCollection) {

    }
}
