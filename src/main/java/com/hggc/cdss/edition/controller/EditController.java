package com.hggc.cdss.edition.controller;


import com.hggc.cdss.edition.bean.MyMessage;
import com.hggc.cdss.edition.service.EditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
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
        mv.addObject("pdfPath","/guidelines/"+disease+"/"+disease+".pdf");
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
    public @ResponseBody MyMessage saveNetworkToDB(@RequestParam("disease")String disease, @RequestParam("task_network")String task_network){
        MyMessage msg = new MyMessage();
        try{
            editService.updateNetwork(disease,task_network);
        }catch (Exception indexOut) {
            msg.setCode(500);
            msg.setInfo("一次只能删除一个节点");
            //如果原来的更新出现问题，就把之间的旧值重新赋值回去
            indexOut.printStackTrace();
        }
        return msg;
    }


    /**
     * 通过节点id获取该节点的全部信息
     * @param nodeId
     * @return
     */
    @RequestMapping("/getNodeInfoByDiseaseAndId")
    public @ResponseBody Map getNodeInfoByDiseaseAndId(@RequestParam("nodeId")int nodeId,@RequestParam("disease")String disease)throws Exception {
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
        msg.setInfo(newNodeInfo);
        editService.updateNodeInfo(disease,nodeId,newNodeInfo);
        return msg;
    }


    /**
     * 得到所有enquiry任务和decision任务的信息
     * @return
     */
    @RequestMapping("/getEnquiryAndDecision")
    public @ResponseBody List<Map<String,Object>> getEnquiryAndDecision(@RequestParam("diseaseName") String diseaseName) throws Exception {
        System.out.println(diseaseName);
        return editService.getEnquiryAndDecision(diseaseName);
    }






}
