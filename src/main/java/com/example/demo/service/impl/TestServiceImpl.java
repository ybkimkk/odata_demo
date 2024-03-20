package com.example.demo.service.impl;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.TestEntity;
import com.example.demo.entity.common.R;
import com.example.demo.mapper.TestMapper;
import com.example.demo.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        return testMapper.selectByMap(arg);
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
}
