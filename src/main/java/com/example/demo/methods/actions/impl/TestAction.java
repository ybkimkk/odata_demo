package com.example.demo.methods.actions.impl;

import com.example.demo.anotation.OdataAction;
import com.example.demo.anotation.OdataActionImport;
import com.example.demo.contains.Contains;
import com.example.demo.methods.actions.IAction;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestAction implements IAction {
    @OdataAction(name = "Reset")
    public CsdlAction Reset() {
        List<CsdlParameter> parameters = new ArrayList<>();
        CsdlParameter parameter = new CsdlParameter();
        parameter.setName("ID");
        parameter.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        parameters.add(parameter);

        CsdlAction action = new CsdlAction();
        action.setName("Reset");
        action.setParameters(parameters);

        return action;
    }


    @OdataActionImport(name = "Reset")
    public CsdlActionImport ResetImport() {
        return new CsdlActionImport()
                .setName("Reset")
                .setAction(new FullQualifiedName(Contains.NAME_SPACE, "Reset"));
    }
}
