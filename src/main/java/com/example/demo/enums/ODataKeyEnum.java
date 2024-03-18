package com.example.demo.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ODataKeyEnum {

    FILTER("$filter",  "", "根据表达式的状态返回结果"),
    ORDER_BY("$orderby", "", "根据结果排序"),
    SKIP("$skip", "", "越过结果中的n条数据，常用于分页"),
    TOP("$top", "", "返回结果中的前n条记录，常用于分页"),
    SELECT("$select", "", "选择需要返回的属性"),
    EXPAND("$expand", "", "选择需要返回的属性"),
    COUNT("$inlinecount", "", "向服务器获取符合条件的资源总数");

    public final String KEY;
    public final String KO_DESC;
    public final String CN_DESC;
}
