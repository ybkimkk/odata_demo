package com.example.demo.option;

import com.example.demo.option.common.CommonOption;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
public class TopOption implements CommonOption {
    @Override
    public void filter(UriInfo uriInfo, Map<String, Object> query) throws ODataApplicationException {
        if (Objects.nonNull(uriInfo.getTopOption())) {
            int topNumber = uriInfo.getTopOption().getValue();
            if (topNumber >= 0) {
                query.put("count", topNumber);
            } else {
                throw new ODataApplicationException("Invalid value for $top", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
            }
        }
    }

}
