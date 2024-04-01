package com.example.demo.option;

import com.example.demo.option.common.CommonOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class ExpandOption implements CommonOption {
    @Override
    public void filter(EntityCollectionSerializerOptions.Builder builder, UriInfo uriInfo, Map<String, Object> query) throws ODataApplicationException {
        org.apache.olingo.server.api.uri.queryoption.ExpandOption expandOption = uriInfo.getExpandOption();
        if (Objects.nonNull(expandOption)) {
            List<ExpandItem> expandItems = expandOption.getExpandItems();
            List<String> tableString = new ArrayList<>();
            for (ExpandItem expandItem : expandItems) {
                if (expandItem.isStar()) {
                    log.info("ExpandOption.filter expandItem.isStar() :{}", uriInfo.asUriInfoResource());
                    //TODO   ????????? 什么情况下会进入 待研究
                } else {
                    List<UriResource> uriResourceParts = expandItem.getResourcePath().getUriResourceParts();
                    for (UriResource uriResourcePart : uriResourceParts) {
                        if (uriResourcePart instanceof UriResourceNavigation) {
                            EdmNavigationProperty property = ((UriResourceNavigation) uriResourcePart).getProperty();
                            if (Objects.nonNull(property)) {
                                String tableName = property.getName();
                                tableString.add(tableName);
                            }
                        }
                    }
                }
            }
            query.put("expand", tableString);
        }
    }
}
