package com.hggc.cdss.execution.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.hggc.cdss.execution.service.ExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
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
    public ModelAndView runTaskNetwork(@RequestParam("diseaseName")String diseaseName) throws Exception{
        ModelAndView mv = new ModelAndView();
        mv.addObject("disease",diseaseName);
        //初始化运行环境
        executionService.initEnv(diseaseName);
        mv.setViewName("runTaskNetwork");
        //return "runTaskNetwork";
        return mv;
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
     * 用户提交自己的答案
     * 那么就代表这个任务已经完成
     * @param resultStr
     * @return
     * @throws Exception
     */
    @RequestMapping("/submitEnquiryResult")
    public @ResponseBody Map<String,Object> submitEnquiryResult(@RequestParam("resultList") String resultStr,@RequestParam("currentTaskId")int nodeId) throws Exception{
        System.out.println(resultStr);
        //将字符串转换成对象数组
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionLikeType type = objectMapper.getTypeFactory().constructCollectionType(List.class,Map.class);
        List<Map> resultArr = objectMapper.readValue(resultStr,type);
        System.out.println(resultArr);
        //将用户回答的结果存入数据库
        executionService.saveEnuiryResult(resultArr);
        //该任务已经完成，更改对应节点状态表的信息
        executionService.updateStatusOfNodeByNodeId(nodeId,"completed");
        //获取下一个要执行的任务，返回给前端
        return executionService.getNextTasks(nodeId);
    }


    /**
     * 用户提交action任务
     * @param nodeId
     * @return
     */
    @RequestMapping("/submitActionTask")
    public @ResponseBody Map<String,Object> submitActionTask(@RequestParam("currentTaskId") int nodeId)throws Exception {
        //该节点已经完成，更改对应节点状态表中的信息
        executionService.updateStatusOfNodeByNodeId(nodeId,"completed");
        //获取下一个要执行的任务，返回给前端
        return executionService.getNextTasks(nodeId);
    }


    /**
     * 用户提交plan任务
     * @param nodeId
     * @return
     * @throws Exception
     */
    @RequestMapping("/submitPlanTask")
    public @ResponseBody Map<String,Object> submitPlanTask(@RequestParam("currentTaskId") int nodeId)throws Exception {
        //该节点已经完成，更改对应节点状态表中的信息
        executionService.updateStatusOfNodeByNodeId(nodeId,"completed");
        //获取下一个要执行的任务，返回给前端
        return executionService.getNextTasks(nodeId);
    }


    /**
     * 用户提交decision任务
     * @param decisionResult
     * @param nodeId
     * @return
     * @throws Exception
     */
    @RequestMapping("/submitDecisionTask")
    public @ResponseBody Map<String,Object> submitDecisionTask(@RequestParam("decisionResult") String decisionResult,@RequestParam("currentNodeId") int nodeId) throws Exception{
        System.out.println(decisionResult);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> decisionResultMap = objectMapper.readValue(decisionResult, Map.class);
        //将结果存入数据库
        executionService.saveDecisionResult(decisionResultMap);
        //将这个节点的状态改变一下
        executionService.updateStatusOfNodeByNodeId(nodeId,"completed");
        //返回下一个任务
        return executionService.getNextTasks(nodeId);
    }


    /**
     * 得到包含节点状态的任务网络
     * @param disease
     * @return
     * @throws Exception
     */
    @RequestMapping("/getNodeStatusAndTaskNetwork")
    public @ResponseBody Map<String,Object> getNodeStatusAndTaskNetwork(@RequestParam("disease") String disease) throws Exception{
        Map<String,Object> result = new HashMap<>();
        result = executionService.getNodeStatusAndTaskNetwork(disease);
        return result;
    }






}
