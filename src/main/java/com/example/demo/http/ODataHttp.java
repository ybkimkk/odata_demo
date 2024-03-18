package com.example.demo.http;

import com.example.demo.entity.response.ResponseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "ODataHttp", url = "${odata.url}")
public interface ODataHttp {

    @GetMapping("Northwind/Northwind.svc/Products/")
    String getList(@SpringQueryMap Object param);


    @GetMapping("Northwind/Northwind.svc/Products/$count")
    String getCount();
}
