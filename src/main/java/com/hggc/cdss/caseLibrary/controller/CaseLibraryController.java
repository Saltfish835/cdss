package com.hggc.cdss.caseLibrary.controller;

import com.hggc.cdss.Utils.ImageTool;
import com.hggc.cdss.caseLibrary.service.CaseLibraryService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cdss")
public class CaseLibraryController {


    @Autowired
    CaseLibraryService caseLibraryService;

    /**
     * 跳转到显示病例库的页面
     * @return
     * @throws Exception
     */
    @RequestMapping("/jumpToCaseLibrary")
    public ModelAndView jumpToCaseLibrary() throws Exception{
        ModelAndView mv = new ModelAndView();
        //查出所有病例，显示到前端
        List<Map> caseList = caseLibraryService.getAllCase();
        mv.addObject("caseList",caseList);
        mv.setViewName("caseLibrary");
        return mv;
    }


    /**
     * 通过case_id得到一个case的详细信息
     * @param caseId
     * @return
     * @throws Exception
     */
    @RequestMapping("/getCaseByCaseId")
    public @ResponseBody Map<String,Object> getCaseByCaseId(@RequestParam("caseId") String caseId) throws Exception{
        Map<String,Object> patientCase = new HashMap<>();
        patientCase = caseLibraryService.getCaseByCaseId(caseId);
        return patientCase;
    }


    /**
     * 根据图片的地址，来获取图片
     * @param addr
     * @param response
     */
    @ResponseBody
    @RequestMapping("/getImg")
    public void getImg(@Param("addr")String addr, HttpServletResponse response) throws Exception{
        BufferedImage img = new BufferedImage(300, 150, BufferedImage.TYPE_INT_RGB);
        img = ImageTool.getInputStream(addr);
        if(img==null){
            throw new RuntimeException("打印图片异常：com.controller.Business_Ctrl.getImg(String, HttpServletResponse)");
        }
        if(img!=null){
            try {
                ImageIO.write(img, "JPG", response.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("打印异常:com.controller.Business_Ctrl.getImg(String, HttpServletResponse)");
            }
        }
    }



}
