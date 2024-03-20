package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.TestEntity;
import com.example.demo.entity.common.R;
import com.example.demo.mapper.TestMapper;
import com.example.demo.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jinyongbin
 * @since 2024-03-19 13:35:48
 */

@Service("testService")
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {


    private final TestMapper testMapper;

    @Override
    public R<List<TestEntity>> selectByCondition(TestEntity request) throws NullPointerException {
        QueryWrapper<TestEntity> testEntityQueryWrapper = new QueryWrapper<>();
        List<TestEntity> test = testMapper.selectList(testEntityQueryWrapper);

        return R.ok(test);
    }


}
