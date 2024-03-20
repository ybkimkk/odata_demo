package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;


/**
 * @author jinyongbin
 * @since  2024-03-19 13:35:48
 */

@Data
@TableName("test")
public class TestEntity {
    @TableId(type = IdType.AUTO)
    private Long ID;
    private String NAME;
    private Long AGE;
    private String PRICE;
}

