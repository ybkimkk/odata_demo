package com.example.demo.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.TypeReference;
import com.alibaba.fastjson2.JSON;
import com.example.demo.entity.common.R;
import com.example.demo.entity.request.RequestEntity;
import com.example.demo.entity.response.ResponseEntity;
import com.example.demo.http.ODataHttp;
import com.example.demo.service.ODataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ODataServiceImpl implements ODataService {

    private final ODataHttp oDataHttp;

    @Override
    public R<List<ResponseEntity>> getList(RequestEntity requestEntity) {
        String list = oDataHttp.getList(JSON.parseObject(JSON.toJSONString(requestEntity)));
        List<ResponseEntity> convert = Convert.convert(new TypeReference<List<ResponseEntity>>() {}, JSON.parseObject(list).get("value"));
        return R.ok(convert);
    }
}
