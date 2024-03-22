package com.example.demo.processor;

import cn.hutool.core.util.StrUtil;
import com.example.demo.service.CommonService;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommonEntityProcessor {

    @Resource
    protected ApplicationContext applicationContext;
    protected static Map<String, CommonService> SERVICE_MAP = new HashMap<>();

    @PostConstruct
    public void init() {
        Map<String, CommonService> beansOfType = applicationContext.getBeansOfType(CommonService.class);
        for (Map.Entry<String, CommonService> entry : beansOfType.entrySet()) {
            CommonService commonService = entry.getValue();
            SERVICE_MAP.put(StrUtil.upperFirst(entry.getKey().replace("Service", StrUtil.EMPTY)), commonService);
        }
    }


    protected CommonService getService(String db) {
        return SERVICE_MAP.get(db);
    }

    protected EntityCollection getEntityCollection(List<?> list) {
        EntityCollection retEntitySet = new EntityCollection();
        try {
            for (Object object : list) {
                Class<?> aClass = object.getClass();
                Field[] declaredFields = aClass.getDeclaredFields();
                retEntitySet.getEntities().add(editEntityValue(object, declaredFields));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return retEntitySet;
    }

    private Entity editEntityValue(Object object, Field[] declaredFields) throws IllegalAccessException {
        Entity entity = new Entity();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            entity.addProperty(new Property(null, field.getName(), ValueType.PRIMITIVE, field.get(object)));
        }
        return entity;
    }

    protected Map<String, Object> getMapByEntity(Entity entity) {
        Map<String, Object> stringObjectMap = new HashMap<>();
        for (Property property : entity.getProperties()) {
            stringObjectMap.put(property.getName(), property.getValue());
        }
        return stringObjectMap;
    }

}
