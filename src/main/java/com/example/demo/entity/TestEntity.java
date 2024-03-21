package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.entity.common.CommonEntity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.EqualsAndHashCode;


/**
 * @author jinyongbin
 * @since  2024-03-19 13:35:48
 */

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("test")
public class TestEntity extends CommonEntity {
    @TableId(type = IdType.AUTO)
    private Long ID;
    private String NAME;
    private Long AGE;
    private String PRICE;
}

