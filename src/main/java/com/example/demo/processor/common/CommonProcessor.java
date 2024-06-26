package com.example.demo.processor.common;

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
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.annotation.Annotation;
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
    private static final Map<Class<?>, Method[]> CACHED_METHODS = new HashMap<>();
    private static final Map<String, CsdlEntityType> ENTITY_TYPE_MAP = new HashMap<>();


    @PostConstruct
    public void init() {
        //init service map
        Map<String, ?> serviceMap = applicationContext.getBeansOfType(ICommonService.class);
        for (Map.Entry<String, ?> entry : serviceMap.entrySet()) {
            ICommonService<?> ICommonService = (ICommonService<?>) entry.getValue();
            String key = cutClassStr(entry.getKey(), "Service");
            key = editOdataMapKey(key);
            SERVICE_MAP.put(key, ICommonService);
        }

        //init odata entity
        Map<String, IEntity> entityMap = applicationContext.getBeansOfType(IEntity.class);
        for (Map.Entry<String, IEntity> entry : entityMap.entrySet()) {
            IEntity iEntity = entry.getValue();
            String className = cutClassStr(entry.getKey(), "Entity");
            className = editOdataMapKey(className);
            ENTITY_MAP.put(className, iEntity);
            ENTITY_TYPE_MAP.put(className, iEntity.getEntityType());
        }

        //init function map
        Map<String, IFunction> functionMap = applicationContext.getBeansOfType(IFunction.class);
        for (Map.Entry<String, IFunction> entry : functionMap.entrySet()) {
            FUNCTION_MAP.put(StrUtil.upperFirst(entry.getKey().replace("Function", StrUtil.EMPTY)), entry.getValue());
        }

    }

    private String cutClassStr(String className, String cutStr) {
        return className.replace(cutStr, StrUtil.EMPTY);
    }


    public ICommonService<?> getService(String db) {
        return SERVICE_MAP.get(db);
    }

    public Map<String, ICommonService<?>> getService() {
        return SERVICE_MAP;
    }


    //TODO
    public void doAction(String actionName, Map<String, String> params) {
        Object odataMethods = getOdataMethods(actionName, ICommonService.class, OdataDoAction.class, params);
    }

    public List<CsdlEntityType> getEntityType(String typeName) {
        if (Objects.nonNull(typeName)) {
            return Collections.singletonList(ENTITY_TYPE_MAP.get(typeName));
        }
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        ENTITY_TYPE_MAP.forEach((x, y) -> {
            entityTypes.add(y);
        });

        return entityTypes;
    }

    public List<CsdlEntitySet> getEntitySet(String typeName) {
        if (Objects.nonNull(typeName)) {
            return Collections.singletonList(ENTITY_MAP.get(typeName).getEntitySet());
        }
        List<CsdlEntitySet> csdlEntitySets = new ArrayList<>();
        ENTITY_MAP.forEach((x, y) -> {
            csdlEntitySets.add(y.getEntitySet());
        });

        return csdlEntitySets;
    }

    public List<CsdlAction> getAction(String actionName) {
        Object odataMethods = getOdataMethods(actionName, IAction.class, OdataAction.class, null);

        return Convert.convert(new TypeReference<List<CsdlAction>>() {
        }, odataMethods);
    }

    public List<CsdlActionImport> getActionImport(String actionName) {
        Object odataMethods = getOdataMethods(actionName, IAction.class, OdataActionImport.class, null);
        return Convert.convert(new TypeReference<List<CsdlActionImport>>() {
        }, odataMethods);
    }


    /**
     * 反射方法 包括但不限于 OdataActionImport  OdataAction   OdataDoAction
     *
     * @param actionName      动作名字
     * @param clazz
     * @param annotationClass 注解方法
     * @param params          执行参数
     * @return
     */
    private List<Object> getOdataMethods(String actionName, Class<?> clazz, Class<? extends Annotation> annotationClass, Map<String, String> params) {
        Map<String, ?> iActionClass = applicationContext.getBeansOfType(clazz);
        List<Object> objects = new ArrayList<>();
        try {
            for (Map.Entry<String, ?> entry : iActionClass.entrySet()) {
                Object value = entry.getValue();
                Method[] methods = getOrCacheMethods(value.getClass());
                for (Method method : methods) {
                    if (method.isAnnotationPresent(annotationClass)) {
                        Annotation annotation = method.getAnnotation(annotationClass);
                        //不需要从springboot 实例对象获取
                        if (annotation instanceof OdataActionImport || annotation instanceof OdataAction) {
                            objects.add(method.invoke(value.getClass().newInstance()));
                            //获取到了指定action就直接跳出否则 全部获取
                            if (StrUtil.isNotEmpty(actionName)) {
                                break;
                            }
                        }

                        //需要从springboot 实例对象获取
                        if (annotation instanceof OdataDoAction) {
                            objects.add(method.invoke(value, params));
                            break;
                        }
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            log.error("获取方法失败:{}", JSON.toJSONString(e));
            throw new RuntimeException(e);
        }
        return objects;
    }

    //此处可以进一步优化
    private Method[] getOrCacheMethods(Class<?> clazz) {
        synchronized (CACHED_METHODS) {
            if (!CACHED_METHODS.containsKey(clazz)) {
                Method[] methods = clazz.getDeclaredMethods();
                CACHED_METHODS.put(clazz, methods);
            }
            return CACHED_METHODS.get(clazz);
        }
    }

    public IFunction getFunction(String db) {
        return FUNCTION_MAP.get(db);
    }

    private String editOdataMapKey(String key) {
        return StrUtil.toUnderlineCase(key).toUpperCase();
    }

}
