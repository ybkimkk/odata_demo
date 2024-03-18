package com.example.demo.controller;


import com.example.demo.entity.common.R;
import com.example.demo.entity.request.RequestEntity;
import com.example.demo.entity.response.ResponseEntity;
import com.example.demo.service.ODataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final ODataService oDataService;

    @PostMapping("test")
    public R<List<ResponseEntity>> test(@RequestBody RequestEntity requestEntity){
        return oDataService.getList(requestEntity);
    }
}
