package com.example.demo.service;

import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;

import java.util.List;
import java.util.Map;

public interface CommonService<T> {
    List<T> selectByCondition(Map<String, Object> arg) throws NullPointerException;

    T insert(Map<String, Object> arg) throws NullPointerException;

    T update(Map<String, Object> arg) throws NullPointerException;

    int delete(String id) throws NullPointerException;

    List<CsdlNavigationProperty> getNavigation();
    List<CsdlNavigationPropertyBinding>  getPath();
}
