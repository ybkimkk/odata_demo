package com.example.demo.processor;

import cn.hutool.core.convert.Convert;
import com.example.demo.processor.common.CommonProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.data.Parameter;
import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.ActionEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.ActionEntityProcessor;
import org.apache.olingo.server.api.processor.ActionVoidProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ActionProcessor implements ActionVoidProcessor, ActionEntityCollectionProcessor, ActionEntityProcessor {

    private OData odata;

    private ServiceMetadata serviceMetadata;

    @Resource
    private CommonProcessor commonProcessor;

    @Override
    public void init(final OData odata, final ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void processActionEntityCollection(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {
        log.info("processActionEntityCollection");
    }

    @Override
    public void processActionEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType, ContentType contentType1) throws ODataApplicationException, ODataLibraryException {
        log.info("processActionEntity");
    }

    @Override
    public void processActionVoid(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType) throws ODataApplicationException, ODataLibraryException {
        EdmAction edmAction = ((UriResourceAction) uriInfo.asUriInfoResource().getUriResourceParts().get(0)).getAction();
        //先拿一手action名
        String actionName = edmAction.getName();

        //在拿一手参数名
        ODataDeserializer deserializer = odata.createDeserializer(contentType);
        Map<String, Parameter> actionParameter = deserializer.actionParameters(oDataRequest.getBody(), edmAction).getActionParameters();

        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, Parameter> entry : actionParameter.entrySet()) {
            params.put(entry.getKey(), Convert.toStr(entry.getValue().getValue()));
        }
        commonProcessor.doAction(actionName,params);

        log.info("processActionVoid");
        oDataResponse.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

    }
}
