package com.example.demo.processor.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.example.demo.anotation.OdataAction;
import com.example.demo.anotation.OdataActionImport;
import com.example.demo.anotation.OdataDoAction;
import com.example.demo.entity.common.IEntity;
import com.example.demo.methods.actions.IAction;
import com.example.demo.methods.functionns.IFunction;
import com.example.demo.service.common.ICommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Component
@Slf4j
public class CommonProcessor {

    @Resource
    protected ApplicationContext applicationContext;
    protected static Map<String, ICommonService<?>> SERVICE_MAP = new HashMap<>();
    protected static Map<String, IEntity> ENTITY_MAP = new HashMap<>();
    protected static Map<String, IFunction> FUNCTION_MAP = new HashMap<>();

    @PostConstruct
    public void init() {
        //init service map
        Map<?, ?> serviceMap = applicationContext.getBeansOfType(ICommonService.class);
        for (Map.Entry<?, ?> entry : serviceMap.entrySet()) {
            ICommonService<?> ICommonService = (ICommonService<?>) entry.getValue();
            String key = StrUtil.toString(entry.getKey());
            SERVICE_MAP.put(StrUtil.upperFirst(key.replace("Service", StrUtil.EMPTY)), ICommonService);
        }

        //init entity map
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory(resolver);
        String entityPath = ClassUtils.convertClassNameToResourcePath("com.example.demo.entity") + "/*.class";
        try {
            for (org.springframework.core.io.Resource resource : resolver.getResources(entityPath)) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                String className = metadataReader.getClassMetadata().getClassName();
                Class<?> aClass = Class.forName(className);
                className = className.substring(className.lastIndexOf('.') + 1).replace("Entity", StrUtil.EMPTY);
                ENTITY_MAP.put(className, (IEntity) aClass.getConstructor().newInstance());
            }
        } catch (IOException e) {
            log.error("getSchemas error :{}", e.toString());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

//
        //init function map
        Map<String, IFunction> functionMap = applicationContext.getBeansOfType(IFunction.class);
        for (Map.Entry<String, IFunction> entry : functionMap.entrySet()) {
            FUNCTION_MAP.put(StrUtil.upperFirst(entry.getKey().replace("Function", StrUtil.EMPTY)), entry.getValue());
        }

    }


    public ICommonService<?> getService(String db) {
        return SERVICE_MAP.get(db);
    }

    public Map<String, ICommonService<?>> getService() {
        return SERVICE_MAP;
    }

    public void doAction(String actionName, Map<String, String> params) {
        Object odataMethods = getOdataMethods(actionName, ICommonService.class, OdataDoAction.class, params);

    }

    public IEntity getEntity(String db) {
        return ENTITY_MAP.get(db);
    }

    public Map<String, IEntity> getEntity() {
        return ENTITY_MAP;
    }

    public List<CsdlAction> getAction() {
        return getAction(null);
    }

    public List<CsdlAction> getAction(String actionName) {
        Object odataMethods = getOdataMethods(actionName, IAction.class, OdataAction.class, null);
        List<CsdlAction> convert = Convert.convert(new TypeReference<List<CsdlAction>>() {
        }, odataMethods);

        if (CollUtil.isNotEmpty(convert)) {
            return convert;
        }
        return null;
    }

    public List<CsdlActionImport> getActionImport(String actionName) {
        Object odataMethods = getOdataMethods(actionName, IAction.class, OdataActionImport.class, null);
        List<CsdlActionImport> convert = Convert.convert(new TypeReference<List<CsdlActionImport>>() {
        }, odataMethods);
        if (CollUtil.isNotEmpty(convert)) {
            return convert;
        }
        return null;
    }

    public List<CsdlActionImport> getActionImports() {
        return this.getActionImport(null);
    }


    private List<Object> getOdataMethods(String actionName, Class<?> clazz, Class<? extends Annotation> annotationClass, Map<String, String> params) {
        Map<String, ?> iActionClass = applicationContext.getBeansOfType(clazz);
        List<Object> objects = new ArrayList<>();
        try {
            for (Map.Entry<String, ?> entry : iActionClass.entrySet()) {
                Object value = entry.getValue();
                Method[] methods = value.getClass().getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(annotationClass)) {

                        if (StrUtil.isBlank(actionName)) {
                            objects.add(method.invoke(value.getClass().newInstance()));
                        } else {
                            Annotation annotation = method.getAnnotation(annotationClass);
                            Method actionNameMethod = annotationClass.getMethod("name");
                            String name = (String) actionNameMethod.invoke(annotation);
                            if (actionName.equals(name)) {
                                if (Objects.nonNull(params)) {
                                    objects.add(method.invoke(value, params));
                                } else {
                                    objects.add(method.invoke(value.getClass().newInstance()));
                                }

                                break;
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                 NoSuchMethodException e) {
            log.error("获取方法失败:{}",JSON.toJSONString(e));
            throw new RuntimeException(e);
        }
        return objects;
    }


    public IFunction getFunction(String db) {
        return FUNCTION_MAP.get(db);
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


}
