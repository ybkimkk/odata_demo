package com.example.demo.service.impl;

import cn.hutool.core.convert.Convert;
import com.example.demo.contains.Contains;
import com.example.demo.entity.TestItemEntity;
import com.example.demo.mapper.TestItemMapper;
import com.example.demo.service.TestItemService;
import lombok.RequiredArgsConstructor;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jinyongbin
 * @since 2024-03-21 11:57:19
 */

@Service("testItemService")
@RequiredArgsConstructor
public class TestItemServiceImpl implements TestItemService {


    private final TestItemMapper testItemMapper;


    @Override
    public List<TestItemEntity> selectByCondition(Map<String, Object> arg) throws NullPointerException {
       TestItemEntity convert = Convert.convert(TestItemEntity.class, arg);
        return testItemMapper.selectByCondition(convert);
    }

    @Override
    public TestItemEntity insert(Map<String, Object> arg) throws NullPointerException {
        return null;
    }

    @Override
    public TestItemEntity update(Map<String, Object> arg) throws NullPointerException {
        return null;
    }

    @Override
    public int delete(String id) throws NullPointerException {
        return 0;
    }

    @Override
    public List<CsdlNavigationProperty> getNavigation() {
        List<CsdlNavigationProperty> navPropList = new ArrayList<>();
        CsdlNavigationProperty navProp = new CsdlNavigationProperty()
                .setName("Test")
                .setType(new FullQualifiedName(Contains.NAME_SPACE, "Test"))
                .setNullable(true)
                .setPartner("TestItem");
        navPropList.add(navProp);
        return navPropList;
    }

    @Override
    public List<CsdlNavigationPropertyBinding> getPath() {
        return null;
    }
}
