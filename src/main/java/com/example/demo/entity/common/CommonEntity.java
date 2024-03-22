package com.example.demo.entity.common;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CommonEntity {
    private Long offset;
    private Long count;
    private String orderBy;
    private String filter;
    private List<Map<String, Object>> join;
}
