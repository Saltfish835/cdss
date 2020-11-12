package com.hggc.cdss;

import com.hggc.cdss.Utils.DSATool;
import com.hggc.cdss.Utils.ExecuteStringAsCodeUtils;
import com.hggc.cdss.caseLibrary.service.CaseLibraryService;
import com.hggc.cdss.edition.service.EditService;
import com.hggc.cdss.execution.component.Loader;
import com.hggc.cdss.execution.service.ExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileWriter;
import java.util.Map;

@SpringBootTest
class CdssApplicationTests {

    @Autowired
    Loader loader;

    @Autowired
    ExecutionService executionService;

    @Autowired
    ExecuteStringAsCodeUtils executeStringAsCodeUtils;

    @Autowired
    EditService editService;

    @Autowired
    CaseLibraryService caseLibraryService;


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


//        String express = "(money >= 100)&&(sex.equals(\"男\"))";
//        Map<String,Object> parameter = new HashMap<>();
//        parameter.put("sex","男");
//        parameter.put("money","1001");
//        parameter.put("age",21);
//        Object result = executeStringAsCodeUtils.executeString(express,parameter);
//        System.out.println(result);

        Map taskNetwork = loader.getGuideline("COVID-19");
        System.out.println(taskNetwork.get("task_network"));


    }



    @Test
    void testDSA() {
        DSATool dsaTool = new DSATool();
        DSATool my = new DSATool();
        //生成秘钥，产生公钥私钥两个文件myprikey.txt,mypubket.txt
        my.key();
        //对传入的字符串内容进行签名，读取myprikey.txt，签名内容写入mysign.txt
        my.sign("我的信息");
        //验证,接受要验证的字符串内容，读取mypubket.txt，mysign.txt，验证成功返回true，失败返回false
        boolean v  = my.verify("我的信息");
        // TODO 自动生成的构造函数存根
        System.out.println("验证结果:"+v);
    }


    @Test
    void testGetGuidelineByDiseaseName() throws Exception{
        Map guidelines = editService.getGuidelineByDiseaseName("COVID-19");
        System.out.println(guidelines);
        //先把指南写入磁盘
        FileWriter writer = new FileWriter("COVID-19_guideline.txt");
        writer.write(guidelines.toString());
        writer.flush();
        writer.close();
    }

    @Test
    void testGetAllCase() throws Exception{
        System.out.println(caseLibraryService.getAllCase());
    }

    void testGetImg() {

    }
}
