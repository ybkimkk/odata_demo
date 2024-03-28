package com.example.demo.methods.functionns.impl;

import com.example.demo.methods.functionns.IFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestFunction implements IFunction {
    @Override
    public List<CsdlFunction> getFunctions() {
        return null;
    }

    @Override
    public CsdlFunctionImport getFunctionImport() {
        return null;
    }
}
