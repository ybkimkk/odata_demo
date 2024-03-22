package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.entity.common.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author jinyongbin
 * @since  2024-03-21 11:57:19
 */

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("test_item")
public class TestItemEntity extends CommonEntity {
    @TableId(type = IdType.AUTO)
    private Long ID;
    private String NAME;
    private Long TEST_ID;
}

