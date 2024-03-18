package com.example.demo.entity.request;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class RequestEntity {
    
    @JSONField(name="$filter")
    private String filter;
    @JSONField(name="$orderby")
    private String orderBy;
    @JSONField(name="$skip")
    private Integer skip;
    @JSONField(name="$top")
    private Integer top;
    @JSONField(name="$select")
    private String select;
    @JSONField(name="$expand")
    private String expand;
    @JSONField(name="$inlinecount")
    private String count;
    @JSONField(name="$format")
    private String format = "json";
}
