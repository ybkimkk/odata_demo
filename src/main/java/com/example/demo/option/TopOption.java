package com.example.demo.option;

import com.example.demo.option.common.CommonOption;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.uri.UriInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class TopOption implements CommonOption {
    @Override
    public List<?> filter(EntityCollectionSerializerOptions.Builder builder, UriInfo uriInfo, List<?> list) throws ODataApplicationException {
        org.apache.olingo.server.api.uri.queryoption.TopOption topOption = uriInfo.getTopOption();
        if (topOption != null) {
            int topNumber = topOption.getValue();
            if (topNumber >= 0) {
                if (topNumber <= list.size()) {
                    list = list.subList(0, topNumber);
                }  // else the client has requested more entities than available => return what we have
            } else {
                throw new ODataApplicationException("Invalid value for $top", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
            }
        }
        return list;
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
