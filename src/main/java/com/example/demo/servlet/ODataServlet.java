package com.example.demo.servlet;

import com.example.demo.processor.ActionProcessor;
import com.example.demo.processor.BatchProcessor;
import com.example.demo.processor.CollectionProcessor;
import com.example.demo.processor.PrimitiveProcessor;
import com.example.demo.processor.common.EdmProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityProcessor;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@WebServlet(name = "ODataServlet", urlPatterns = "/Aspn.svc/*")
@Slf4j
public class ODataServlet extends HttpServlet {

    @Resource
    private CollectionProcessor collectionProcessor;

    @Resource
    private EntityProcessor entityProcessor;

    @Resource
    private PrimitiveProcessor primitiveProcessor;

    @Resource
    private BatchProcessor batchProcessor;

    @Resource
    private ActionProcessor actionProcessor;
    @Resource
    private EdmProvider edmProvider;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {

        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(edmProvider, new ArrayList<>());

        try {
            ODataHttpHandler handler = odata.createHandler(edm);
            handler.register(collectionProcessor);
            handler.register(entityProcessor);
            handler.register(primitiveProcessor);
            handler.register(actionProcessor);
            handler.register(batchProcessor);
            handler.process(req, resp);
        } catch (RuntimeException e) {
            log.error("Server Error occurred in ExampleServlet", e);
        }
    }

}
