package com.example.demo.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ODataFeignInterceptor implements RequestInterceptor {

    @Value("${odata.url}")
    private String url;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        if (requestTemplate.feignTarget().url().equals(url)) {
            System.out.println("request odata");
        }
    }
}
