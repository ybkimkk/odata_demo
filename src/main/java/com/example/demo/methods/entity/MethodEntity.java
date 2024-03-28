package com.example.demo.methods.entity;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class MethodEntity {
    private Method method;
    private Class<?> clazz;
}
