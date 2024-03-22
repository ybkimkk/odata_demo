package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.TestEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestMapper extends BaseMapper<TestEntity> {
    Long insertByCondition(@Param("arg") TestEntity arg);

    Long updateBatch(@Param("arg") List<TestEntity> arg);

    List<TestEntity> selectByCondition(@Param("arg") TestEntity arg);
}
