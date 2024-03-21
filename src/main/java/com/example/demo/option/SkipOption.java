package com.example.demo.option;

import com.example.demo.option.common.CommonOption;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.uri.UriInfo;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class SkipOption implements CommonOption {
    @Override
    public void filter(EntityCollectionSerializerOptions.Builder builder, UriInfo uriInfo, Map<String, Object> query) throws ODataApplicationException {
        org.apache.olingo.server.api.uri.queryoption.SkipOption skipOption = uriInfo.getSkipOption();
        if (skipOption != null) {
            int skipNumber = skipOption.getValue();
            if (skipNumber >= 0) {
                query.put("offset", skipNumber);
            } else {
                throw new ODataApplicationException("Invalid value for $skip", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
            }
        }

    }

}
