package com.example.demo.methods.functionns;

import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;

import java.util.List;

public interface IFunction {

    List<CsdlFunction> getFunctions();

    CsdlFunctionImport getFunctionImport();
}
