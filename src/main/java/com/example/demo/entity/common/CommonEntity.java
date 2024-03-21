package com.example.demo.entity.common;

import lombok.Data;

import java.util.List;

@Data
public class CommonEntity {
    private Long offset;
    private Long count;
    private String orderBy;
    private String filter;
}
