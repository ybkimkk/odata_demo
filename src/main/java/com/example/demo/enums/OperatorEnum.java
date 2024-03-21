package com.example.demo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperatorEnum {

    MUL(" mul", " * "),
    DIV(" div ", " / "),
    MOD(" mod ", " % "),
    ADD(" add ", " + "),
    SUB(" sub ", " - "),

    GT(" gt ", " > "),
    GE(" ge ", " >= "),
    LT(" lt ", " < "),
    LE(" le ", " <= "),
    EQ(" eq ", " = "),
    NE(" ne ", " != "),
    AND(" and ", " and "),
    OR(" or ", " or ");

    private final String operate;

    private final String mySqlOperate;
}
