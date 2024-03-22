package com.example.demo.servlet;

import com.example.demo.processor.*;
import com.example.demo.processor.common.EdmProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
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

    private static ODataHttpHandler handler;

    @PostConstruct
    public void init() {
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(edmProvider, new ArrayList<>());
        handler = odata.createHandler(edm);
        handler.register(collectionProcessor);
        handler.register(entityProcessor);
        handler.register(primitiveProcessor);
        handler.register(actionProcessor);
        handler.register(batchProcessor);
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        handler.process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        handler.process(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        handler.process(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        handler.process(req, resp);
    }
}
