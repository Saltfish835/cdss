package com.hggc.cdss;

import com.hggc.cdss.execution.component.Loader;
import com.hggc.cdss.execution.service.ExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CdssApplicationTests {

    @Autowired
    Loader loader;

    @Autowired
    ExecutionService executionService;

    @Test
    void contextLoads() throws Exception{
        //Map net = loader.getGuideline("COVID-19");
        //System.out.println(net);
        executionService.initEnv("COVID-19");
        System.out.println(executionService.getGraph());
//        Graph graph = loader.getGraph("COVID-19");
//        //System.out.println(graph);
//        Map<String,Object> startingPoint = (Map<String,Object>)graph.getStaertingPoint();
//        System.out.println(startingPoint);
//
//        Map<String,Object> secondNode = (Map<String,Object>)graph.getAllSon(graph.convertNodeIdToIndex(-3)).get(0);
//        System.out.println(secondNode);
    }



}
