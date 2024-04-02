package com.example.demo.service.impl;

import cn.hutool.core.convert.Convert;
import com.example.demo.entity.TestItemEntity;
import com.example.demo.mapper.TestItemMapper;
import com.example.demo.service.ITestItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author jinyongbin
 * @since 2024-03-21 11:57:19
 */

@Service("TestItemService")
@RequiredArgsConstructor
public class TestItemServiceImpl implements ITestItemService {


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

}
