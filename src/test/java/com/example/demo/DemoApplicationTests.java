package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() throws ClassNotFoundException, IOException {
        List<String> classes = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory(resolver);
        String packageSearchPath = ClassUtils.convertClassNameToResourcePath("com.example.demo.entity") + "/*.class";


        for (org.springframework.core.io.Resource resource : resolver.getResources(packageSearchPath)) {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
            String className = metadataReader.getClassMetadata().getClassName();
            classes.add(className.substring(className.lastIndexOf('.') + 1));
        }
        System.out.println(classes);
    }

    @Test
    void getClazz() {
        String a = "asdasdb";
        byte[] bytes = a.getBytes();
        int length = IntStream.range(0, bytes.length)
                .map(i -> bytes[i]).distinct().toArray().length;
        System.out.println(length);
    }

}
