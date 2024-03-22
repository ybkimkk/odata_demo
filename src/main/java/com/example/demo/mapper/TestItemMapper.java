package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.TestItemEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author jinyongbin
 * @since  2024-03-21 11:57:19
 */

@Mapper
public interface TestItemMapper extends BaseMapper<TestItemEntity> {
    List<TestItemEntity> selectByCondition(@Param("arg") TestItemEntity arg);

    Integer updateBatch(@Param("arg") List<TestItemEntity> arg);

    Integer insertByCondition(@Param("arg") TestItemEntity arg);
}
