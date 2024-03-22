package com.example.demo.servlet;

import com.example.demo.processor.*;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@WebServlet(name = "ODataServlet", urlPatterns = "/Aspn.svc/*")
public class ODataServlet extends HttpServlet {

    @Autowired
    private ListEntityCollectionProcessor entityCollectionProcessor;

    @Autowired
    private DetailEntityProcessor entityProcessor;

    @Autowired
    private DetailPrimitiveProcessor primitiveProcessor;

    @Autowired
    private DemoBatchProcessor batchProcessor;

    @Autowired
    private InitEdmProvider initEdmProvider;

    private ODataHttpHandler handler;

    @PostConstruct
    public void init() {
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(initEdmProvider, new ArrayList<>());
        handler = odata.createHandler(edm);
        handler.register(entityCollectionProcessor);
        handler.register(entityProcessor);
        handler.register(primitiveProcessor);
//        handler.register(sizeEntityCollectionProcessor);
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
