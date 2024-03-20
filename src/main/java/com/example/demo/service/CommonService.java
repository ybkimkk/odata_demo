package com.example.demo.service;

import com.example.demo.entity.TestEntity;

import java.util.List;
import java.util.Map;

public interface CommonService {
    List<TestEntity> selectByCondition(Map<String, Object> arg) throws NullPointerException;
    TestEntity insert(Map<String, Object> arg) throws NullPointerException;

    TestEntity update(Map<String, Object> arg) throws NullPointerException;
    int delete(String id) throws NullPointerException;
}
