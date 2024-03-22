package com.example.demo.service.common;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;

import java.util.List;
import java.util.Map;

public interface ICommonService<T> {

    List<T> selectByCondition(Map<String, Object> arg) throws NullPointerException;

    T insert(Map<String, Object> arg) throws NullPointerException;

    T update(Map<String, Object> arg) throws NullPointerException;

    int delete(String id) throws NullPointerException;

    //必须继承
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName);

    CsdlEntityType getEntityType(FullQualifiedName entityTypeName);


    List<CsdlAction> getActions(final FullQualifiedName actionName);


    CsdlActionImport getActionImport(final FullQualifiedName entityContainer, final String actionImportName);

    List<CsdlFunction> getFunctions(final FullQualifiedName functionName);

    CsdlFunctionImport getFunctionImport(final FullQualifiedName entityContainer, String functionImportName);

}
