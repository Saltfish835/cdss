package com.hggc.cdss.edition.controller;


import com.hggc.cdss.edition.bean.MyMessage;
import com.hggc.cdss.edition.service.EditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequestMapping("/cdss")
public class EditController {

    @Autowired
    EditService editService;

    /**
     * 用户访问首页的方法
     * @return
     */
    @RequestMapping("/index")
    public String jumpToIndexPage(){
        return "index";
    }


    /**
     * 跳转至图形化编辑界面
     * @param disease
     * @return
     * @throws Exception
     */
    @RequestMapping("/edit")
    public ModelAndView jumpToEditPage(@RequestParam("disease") String disease) throws Exception{
        ModelAndView mv = new ModelAndView();
        //疾病名称是唯一标识
        mv.addObject("disease",disease);
        mv.setViewName("edit");
        return mv;
    }


    /**
     * 通过ajax获得某疾病的任务网络
     * @param disease
     * @return
     * @throws Exception
     */
    @RequestMapping("/getNetworkByDisease")
    public @ResponseBody Map getNetworkByDisease(@RequestParam("disease")String disease)throws Exception {
        Map task_network = editService.findTaskNetworkByDisease(disease);
        return task_network;
    }


    /**
     * 通过疾病名称更新该疾病的任务网络
     * @param disease
     * @param task_network
     * @return
     */
    @RequestMapping("/saveNetworkToDB")
    public @ResponseBody MyMessage saveNetworkToDB(@RequestParam("disease")String disease, @RequestParam("task_network")String task_network) throws Exception{
        System.out.println(disease);
        System.out.println(task_network);
        editService.updateNetwork(disease,task_network);
        MyMessage msg = new MyMessage();
        return msg;
    }


    /**
     * 通过节点id获取该节点的全部信息
     * @param nodeId
     * @return
     */
    @RequestMapping("/getNodeInfoByDiseaseAndId")
    public @ResponseBody Map getNodeInfoByDiseaseAndId(@RequestParam("nodeId")int nodeId,@RequestParam("disease")String disease)throws Exception {
        System.out.println(nodeId);
        System.out.println(disease);
        return editService.getNodeInfoByDiseaseAndId(disease,nodeId);
    }


    /**
     * 更新节点的信息
     * 通过disease和nodeId找到节点，然后通过newNodeInfo全部替换
     * @param disease
     * @param nodeId
     * @param newNodeInfo
     * @return
     */
    @RequestMapping("/updateNodeInfo")
    public @ResponseBody MyMessage updateNodeInfo(@RequestParam("disease") String disease,@RequestParam("nodeId")int nodeId,@RequestParam("newNodeInfo")String newNodeInfo) throws Exception{
        MyMessage msg = new MyMessage();
        System.out.println(disease);
        System.out.println(nodeId);
        System.out.println(newNodeInfo);
        editService.updateNodeInfo(disease,nodeId,newNodeInfo);
        return msg;
    }

}
