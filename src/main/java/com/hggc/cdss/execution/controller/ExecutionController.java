package com.hggc.cdss.execution.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hggc.cdss.execution.service.ExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cdss")
public class ExecutionController {


    @Autowired
    ExecutionService executionService;



    /**
     * 用户点击运行任务网络
     * 跳转到一个新页面执行任务网络
     *
     * @return
     */
    @RequestMapping("/runTaskNetwork")
    public String runTaskNetwork(@RequestParam("diseaseName")String diseaseName) throws Exception{
        //初始化运行环境
        executionService.initEnv(diseaseName);
        return "runTaskNetwork";
    }


    /**
     * 把要执行的第个任务发送给前端
     * 这个方法只执行一次
     * @return
     * @throws Exception
     */
    @RequestMapping("/getFristTask")
    public @ResponseBody Map<String,Object> getFristTask()throws Exception {
        return executionService.getFristTask();
    }


    /**
     * 用户的答案
     * @param result
     * @return
     * @throws Exception
     */
    @RequestMapping("/submitEnquiryResult")
    public @ResponseBody Map<String,Object> submitEnquiryResult(@RequestParam("result") String result) throws Exception{
        System.out.println(result);
        //将字符串转换成对象数组
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,Object> resultArr = new HashMap<String,Object>();
        resultArr = objectMapper.readValue(objectMapper.writeValueAsString(result), Map.class);//将查出的结果封装成map集合
        System.out.println(resultArr);
        return resultArr;
    }

}
