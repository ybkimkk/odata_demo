package com.example.demo.option;

import com.example.demo.option.common.CommonOption;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.uri.UriInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class OrderByOption implements CommonOption {
    @Override
    public void filter(EntityCollectionSerializerOptions.Builder builder, UriInfo uriInfo, Map<String, Object> query) throws ODataApplicationException {
        org.apache.olingo.server.api.uri.queryoption.OrderByOption orderByOption = uriInfo.getOrderByOption();
        if (Objects.nonNull(orderByOption)) {
            query.put("orderBy", orderByOption.getText());
        }
    }
}
