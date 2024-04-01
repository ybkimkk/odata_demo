package com.example.demo.util;

import com.alibaba.fastjson2.JSON;
import com.example.demo.contains.Contains;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class OdataUtil {

    public static FullQualifiedName getAllFullQualifiedName(String name) {
        return new FullQualifiedName(Contains.NAME_SPACE, name);
    }

    public static List<CsdlProperty> getSimpleOdataEntity(String entityName) throws ClassNotFoundException {

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
    public static FullQualifiedName getFullQualifiedName(String fieldType) {
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

    public static EntityCollection getEntityCollection(List<?> list) {
        EntityCollection retEntitySet = new EntityCollection();
        try {
            for (Object object : list) {
                Class<?> aClass = object.getClass();
                Field[] declaredFields = aClass.getDeclaredFields();
                retEntitySet.getEntities().add(editEntityValue(object, declaredFields));
            }
        } catch (IllegalAccessException e) {
            log.error("CommonEntityProcessor.getEntityCollection has error:{},param :{}", e, JSON.toJSONString(list));
        }

        return retEntitySet;
    }

    private static Entity editEntityValue(Object object, Field[] declaredFields) throws IllegalAccessException {
        Entity entity = new Entity();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            entity.addProperty(new Property(null, field.getName(), ValueType.PRIMITIVE, field.get(object)));
        }

        return entity;
    }

    public static Map<String, Object> convertEntityToMap(Entity entity) {
        Map<String, Object> stringObjectMap = new HashMap<>();
        for (Property property : entity.getProperties()) {
            stringObjectMap.put(property.getName(), property.getValue());
        }
        return stringObjectMap;
    }
}
