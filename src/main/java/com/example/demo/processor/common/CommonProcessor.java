package com.example.demo.processor.common;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.example.demo.service.common.ICommonService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CommonProcessor {

    @Resource
    protected ApplicationContext applicationContext;
    protected static Map<String, ICommonService<?>> SERVICE_MAP = new HashMap<>();

    @PostConstruct
    public void init() {
        Map<String, ICommonService> beansOfType = applicationContext.getBeansOfType(ICommonService.class);
        for (Map.Entry<String, ICommonService> entry : beansOfType.entrySet()) {
            ICommonService ICommonService = entry.getValue();
            SERVICE_MAP.put(StrUtil.upperFirst(entry.getKey().replace("Service", StrUtil.EMPTY)), ICommonService);
        }
    }


    protected ICommonService getService(String db) {
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
            log.error("CommonEntityProcessor.getEntityCollection has error:{},param :{}", e, JSON.toJSONString(list));
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

    protected Map<String, Object> convertEntityToMap(Entity entity) {
        Map<String, Object> stringObjectMap = new HashMap<>();
        for (Property property : entity.getProperties()) {
            stringObjectMap.put(property.getName(), property.getValue());
        }
        return stringObjectMap;
    }

    public Map<String, ICommonService<?>> getServiceMap() {
        return SERVICE_MAP;
    }

}
