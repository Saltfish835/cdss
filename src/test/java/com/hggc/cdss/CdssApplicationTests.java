package com.hggc.cdss;

import com.hggc.cdss.Utils.ExecuteStringAsCodeUtils;
import com.hggc.cdss.execution.component.Loader;
import com.hggc.cdss.execution.service.ExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class CdssApplicationTests {

    @Autowired
    Loader loader;

    @Autowired
    ExecutionService executionService;

    @Autowired
    ExecuteStringAsCodeUtils executeStringAsCodeUtils;



    @Test
    void contextLoads() throws Exception{
        //Map net = loader.getGuideline("COVID-19");
        //System.out.println(net);
//        executionService.initEnv("COVID-19");
//        System.out.println(executionService.getGraph());
//        Graph graph = loader.getGraph("COVID-19");
//        //System.out.println(graph);
//        Map<String,Object> startingPoint = (Map<String,Object>)graph.getStaertingPoint();
//        System.out.println(startingPoint);
//
//        Map<String,Object> secondNode = (Map<String,Object>)graph.getAllSon(graph.convertNodeIdToIndex(-3)).get(0);
//        System.out.println(secondNode);


        String express = "(money >= 100)&&(sex.equals(\"男\"))";
        Map<String,Object> parameter = new HashMap<>();
        parameter.put("sex","男");
        parameter.put("money","1001");
        parameter.put("age",21);
        Object result = executeStringAsCodeUtils.executeString(express,parameter);
        System.out.println(result);



    }



}
