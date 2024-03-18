package com.example.demo.service;

import com.example.demo.entity.common.R;
import com.example.demo.entity.request.RequestEntity;
import com.example.demo.entity.response.ResponseEntity;

import java.util.List;

public interface ODataService {
    R<List<ResponseEntity>> getList(RequestEntity requestEntity);
}
