package com.example.demo.service;

import com.example.demo.entity.TestEntity;
import com.example.demo.entity.common.R;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;

import java.util.List;

public interface CommonService {
    R<List<TestEntity>> selectByCondition(TestEntity request) throws NullPointerException;
}
