package com.example.demo.service.common;

import java.util.List;
import java.util.Map;

public interface ICommonService<T> {

    List<T> selectByCondition(Map<String, Object> arg) throws NullPointerException;

    T insert(Map<String, Object> arg) throws NullPointerException;

    T update(Map<String, Object> arg) throws NullPointerException;

    int delete(String id) throws NullPointerException;
}
