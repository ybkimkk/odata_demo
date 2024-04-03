package com.example.demo.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.example.demo.contains.Contains;
import com.example.demo.entity.common.CommonEntity;
import com.example.demo.entity.odata.OdataRequestEntity;
import com.example.demo.option.common.CommonOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.server.api.uri.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

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
            Object o = field.get(object);
            if (Objects.nonNull(o)){
                entity.addProperty(new Property(null, field.getName(), ValueType.PRIMITIVE, o));

            }
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

    public static List<OdataRequestEntity> getEdmHelper(List<UriResource> resourcePaths) {
        List<OdataRequestEntity> odataRequestEntities = new ArrayList<>();
        List<CommonEntity.Join> joins = new ArrayList<>();

        for (UriResource resourcePath : resourcePaths) {
            OdataRequestEntity odataRequestEntity = new OdataRequestEntity();
            if (resourcePath instanceof UriResourceNavigation) {
                UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) resourcePath;
                EdmNavigationProperty property = uriResourceNavigation.getProperty();
                odataRequestEntity.setEntityType(property.getType());
                UriResourceEntitySet path = (UriResourceEntitySet) resourcePaths.get(0);
                EdmEntitySet edmEntitySet = path.getEntitySet();
                EdmEntitySet entitySet = (EdmEntitySet) edmEntitySet.getRelatedBindingTarget(property.getName());
                odataRequestEntity.setEdmEntitySet(entitySet);
            } else {
                UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePath;
                EdmEntitySet entitySet = uriResourceEntitySet.getEntitySet();

                for (UriParameter keyPredicate : uriResourceEntitySet.getKeyPredicates()) {
                    CommonEntity.Join join = new CommonEntity.Join();
                    join.setTable(entitySet.getName());
                    join.setValue(keyPredicate.getText());
                    join.setField(keyPredicate.getName());
                    joins.add(join);
                }
                odataRequestEntity.setEntityType(entitySet.getEntityType());
                odataRequestEntity.setEdmEntitySet(entitySet);
                odataRequestEntity.setJoin(joins);
            }
            odataRequestEntities.add(odataRequestEntity);
        }
        return odataRequestEntities;
    }

    public static OdataRequestEntity getMainSetAndType(List<OdataRequestEntity> edmHelper) {
        return edmHelper.stream()
                .reduce((x, y) -> y)
                .orElse(null);

    }

    public static Map<String, Object> getSqlQuery(UriInfo uriInfo,Map<String, CommonOption> options, List<OdataRequestEntity> edmHelper){
        Map<String, Object> query = new HashMap<>();
        for (CommonOption value : options.values()) {
            value.filter(uriInfo, query);
        }

        List<Map<String, String>> mapList = new ArrayList<>();
        for (OdataRequestEntity odataRequestEntity : edmHelper) {
            if (Objects.nonNull(odataRequestEntity.getJoin())) {
                List<CommonEntity.Join> join = odataRequestEntity.getJoin();
                mapList = join.stream().map(x -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("field", x.getField());
                    map.put("table", StrUtil.toUnderlineCase(x.getTable()));
                    map.put("value", x.getValue());
                    return map;
                }).collect(Collectors.toList());

            }
        }
        query.put("join",mapList);
        return query;
    }
}
