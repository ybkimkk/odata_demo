package com.example.demo.option;

import com.example.demo.enums.OperatorEnum;
import com.example.demo.option.common.CommonOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.uri.UriInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class FilterOption implements CommonOption {
    @Override
    public void filter(EntityCollectionSerializerOptions.Builder builder, UriInfo uriInfo, Map<String, Object> query) throws ODataApplicationException {
        org.apache.olingo.server.api.uri.queryoption.FilterOption filterOption = uriInfo.getFilterOption();
        if (Objects.nonNull(filterOption)) {
            OperatorEnum[] OperatorEnums = OperatorEnum.values();
            String text = filterOption.getText();
            for (OperatorEnum OperatorEnum : OperatorEnums) {
                if (filterOption.getText().contains(OperatorEnum.getOperate())) {
                    text = text.replaceAll(OperatorEnum.getOperate(), OperatorEnum.getMySqlOperate());
                }
            }
            text = getLikeSql(text);
            query.put("filter", text);
            log.error(text);

        }
    }

    private String getLikeSql(String text) {
        Pattern pattern = Pattern.compile("contains\\((.*?),'(.*?)'\\)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String searchSql = matcher.group(1) + " LIKE '" + matcher.group(2) + "%'";
            text = text.replace(matcher.group(), searchSql);
        }

        return text;
    }

}
