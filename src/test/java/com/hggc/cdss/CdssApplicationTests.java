package com.hggc.cdss;

import com.hggc.cdss.edition.service.EditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class CdssApplicationTests {

    @Autowired
    EditService editService;

    @Test
    void contextLoads() throws Exception{
        Map net = editService.getNodeInfoByDiseaseAndId("COVID-19",-3);
        System.out.println(net);
    }



}
