package com.example.demo.processor;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.TestEntity;
import com.example.demo.mapper.TestMapper;
import com.example.demo.service.CommonService;
import com.example.demo.service.TestService;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class CommonEntityProcessor {
    @Autowired
    private TestMapper testMapper;


    protected static Map<String, TestMapper> mapperMap = null;


    protected BaseMapper<?> getMapper(String db) {
        if (Objects.isNull(mapperMap)) {
            mapperMap = new HashMap<>();
            mapperMap.put("Tests", testMapper);
        }

        return mapperMap.get(db);
    }

    protected EntityCollection getEntityCollection(List<?> list) {
        EntityCollection retEntitySet = new EntityCollection();
        Entity entity = new Entity();
        try {
            for (Object o : list) {
                Class<?> aClass = o.getClass();
                Field[] declaredFields = aClass.getDeclaredFields();
                for (Field field : declaredFields) {
                    field.setAccessible(true);
                    entity.addProperty(new Property(null, field.getName(), ValueType.PRIMITIVE, field.get(o)));
                }
                retEntitySet.getEntities().add(entity);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return retEntitySet;
    }

}
