package com.example.demo.option;

import com.example.demo.option.common.CommonOption;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class OrderByOption implements CommonOption {
    @Override
    public void filter( UriInfo uriInfo, Map<String, Object> query) throws ODataApplicationException {
        if (Objects.nonNull(uriInfo.getOrderByOption())) {
            query.put("orderBy", uriInfo.getOrderByOption().getText());
        }
    }
}
