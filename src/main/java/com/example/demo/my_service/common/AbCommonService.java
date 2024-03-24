package com.example.demo.my_service.common;

import com.example.demo.contains.Contains;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AbCommonService {
    protected List<CsdlProperty> getSimpleOdataEntity(String entityName) throws ClassNotFoundException {

        entityName = Contains.PACKAGE + ".entity." + entityName + "Entity";
        Class<?> clazz = Class.forName(entityName);
        List<CsdlProperty> csdlPropertyArrayList = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Class<?> type = field.getType();
            csdlPropertyArrayList.add(
                    new CsdlProperty()
                            .setName(field.getName().toUpperCase())
                            .setType(getFullQualifiedName(type.getName()))
            );
        }
        return csdlPropertyArrayList;
    }


    //未知变量在这里添加
    private FullQualifiedName getFullQualifiedName(String fieldType) {
        switch (fieldType) {
            case "java.math.BigDecimal":
                return EdmPrimitiveTypeKind.Decimal.getFullQualifiedName();
            case "byte":
                return EdmPrimitiveTypeKind.Byte.getFullQualifiedName();
            case "java.util.Date":
                return EdmPrimitiveTypeKind.Date.getFullQualifiedName();
            case "double":
            case "java.lang.Double":
                return EdmPrimitiveTypeKind.Double.getFullQualifiedName();
            case "int":
            case "java.lang.Integer":
                return EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
            case "long":
            case "java.lang.Long":
                return EdmPrimitiveTypeKind.Int64.getFullQualifiedName();
            case "boolean":
            case "java.lang.Boolean":
                return EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
            case "char":
            case "java.lang.String":
            default:
                return EdmPrimitiveTypeKind.String.getFullQualifiedName();
        }
    }

    protected FullQualifiedName getAllFullQualifiedName(String name) {
        return new FullQualifiedName(Contains.NAME_SPACE, name);
    }

}
