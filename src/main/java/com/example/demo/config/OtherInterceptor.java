package com.example.demo.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;


@Configuration
public class OtherInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Collection<String> requestVariables = requestTemplate.getRequestVariables();
        List<String> variables = requestTemplate.variables();
    }
}
