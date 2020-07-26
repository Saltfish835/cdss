package com.hggc.cdss.edition.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hggc.cdss.Utils.DSATool;
import com.hggc.cdss.Utils.ZipMultiFileTool;
import com.hggc.cdss.edition.bean.MyMessage;
import com.hggc.cdss.edition.service.EditService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileWriter;
import java.net.URLEncoder;
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
        mv.addObject("alertFlag",false);
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


    /**
     * 发布指南的方法
     * @param request
     * @param disease
     * @param userAgent
     * @return
     */
    @RequestMapping("/publishGuideline")
    public ResponseEntity<byte[]> publishGuideline(HttpServletRequest request, @RequestParam("disease")String disease, @RequestHeader("User-Agent") String userAgent, Model model) throws Exception{
        //先把原来的文件删除掉
        editService.deleteFiles(disease);
        //得到指南
        Map guideline = editService.getGuidelineByDiseaseName(disease);
        //先把指南写入磁盘
        FileWriter writer = new FileWriter(disease+"_guideline.txt");
        ObjectMapper objectMapper = new ObjectMapper();
        writer.write(objectMapper.writeValueAsString(guideline));
        writer.flush();
        writer.close();//这将把指南变成文件
        //对指南进行签名操作
        DSATool my = new DSATool();
        my.key();//这将会在根目录下生成myprikey.txt,mypubket.txt
        my.sign(objectMapper.writeValueAsString(guideline));//这将会在根目录下生成myinfo.txt
        //把要发布的文件（签名文件和指南）打包成一个文件
        File[] srcFiles = {new File("mypubkey.txt"),new File("mysign.txt"),new File(disease+"_guideline.txt")};
        File zipFile = new File("publishFile.zip");
        ZipMultiFileTool.zipFiles(srcFiles,zipFile);//打包
        //开始准备把文件传给前端
        ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok();
        bodyBuilder.contentLength(zipFile.length());
        bodyBuilder.contentType(MediaType.APPLICATION_OCTET_STREAM);
        String fileName = URLEncoder.encode("publishFile.zip","UTF-8");
        if(userAgent.indexOf("MSIE") > 0) {//如果是IE浏览器
            bodyBuilder.header("Content-Disposition","attachment;filename="+fileName);
        }else {//是其他浏览器
            bodyBuilder.header("Content-Disposition","attachment;filename*=UTF-8''"+fileName);
        }
        return bodyBuilder.body(FileUtils.readFileToByteArray(zipFile));
    }



    @RequestMapping("/loadGuideline")
    public ModelAndView loadGuideline(@RequestParam("files") MultipartFile[] files,@RequestParam("disease")String disease) throws Exception {
        //先清除文件
        editService.deleteFiles(disease);
        //迎接新文件
        ModelAndView mv = new ModelAndView();
        System.out.println(disease);
        if(files.length == 0){
            throw new Exception("请选择要上传的文件");
        }
        for (MultipartFile multipartFile : files) {
            if(multipartFile.isEmpty()){
                throw new Exception("文件上传失败");
            }
            byte[] fileBytes = multipartFile.getBytes();
            FileUtils.writeByteArrayToFile(new File(multipartFile.getOriginalFilename()),fileBytes);
        }
        //把要校验的文件读成字符串
        String guideline = editService.readFile(disease+"_guideline.txt");
        //校验签名
        DSATool my = new DSATool();
        boolean flag = my.verify(guideline);
        mv.addObject("alertFlag",true);//需不需要弹框
        if(flag == true) {
            mv.addObject("verifyStatus",true);
            //校验通过的话，就把上传过来的指南的内容跟新到数据库
            editService.updateDBByUploadGuideline(disease,guideline);
        }else {
            mv.addObject("verifyStatus",false);
        }
        //疾病名称是唯一标识
        mv.addObject("disease",disease);
        mv.addObject("pdfPath","/guidelines/"+disease+"/"+disease+".pdf");
        mv.setViewName("edit");
        return mv;
    }

}
