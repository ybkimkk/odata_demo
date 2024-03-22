package com.example.demo.service.impl;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.contains.Contains;
import com.example.demo.entity.TestEntity;
import com.example.demo.mapper.TestMapper;
import com.example.demo.service.TestService;
import lombok.RequiredArgsConstructor;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author jinyongbin
 * @since 2024-03-19 13:35:48
 */

@Service("testService")
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final TestMapper testMapper;

    @Override
    public List<TestEntity> selectByCondition(Map<String, Object> arg) throws NullPointerException {
        TestEntity testEntity = Convert.convert(TestEntity.class, arg);
        if (Objects.nonNull(testEntity.getOffset()) && Objects.isNull(testEntity.getCount())) {
            testEntity.setCount(testMapper.selectCount(new QueryWrapper<>()));
        }
        return testMapper.selectByCondition(testEntity);
    }

    @Override
    public TestEntity insert(Map<String, Object> arg) throws NullPointerException {
        TestEntity convert = Convert.convert(TestEntity.class, arg);
        testMapper.insert(convert);
        return convert;
    }

    @Override
    public TestEntity update(Map<String, Object> arg) throws NullPointerException {
        TestEntity convert = Convert.convert(TestEntity.class, arg);
        testMapper.updateBatch(Collections.singletonList(convert));
        return convert;
    }

    @Override
    public int delete(String id) throws NullPointerException {
        return testMapper.deleteById(id);
    }

    @Override
    public List<CsdlNavigationProperty> getNavigation() {
        List<CsdlNavigationProperty> navPropList = new ArrayList<>();
        CsdlNavigationProperty navProp = new CsdlNavigationProperty()
                .setName("TestItem")
                .setType(new FullQualifiedName(Contains.NAME_SPACE, "TestItem"))
                .setCollection(true)
                .setPartner("Test");
        navPropList.add(navProp);
        return navPropList;
    }

    @Override
    public List<CsdlNavigationPropertyBinding> getPath() {
        CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
        navPropBinding.setTarget("TestItem");//target entitySet, where the nav prop points to
        navPropBinding.setPath("TestItem"); // the path from entity type to navigation property
        List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<>();
        navPropBindingList.add(navPropBinding);
        return navPropBindingList;
    }
}
