package com.hggc.cdss.Utils;

import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;

public class ImageTool {
    /**
     * 根据图片的地址，返回图片的缓冲流
     * @param addr
     * @return
     */
    public static BufferedImage getInputStream(String addr) throws Exception{
        File path = new File(ResourceUtils.getURL("classpath:").getPath());

        try {
            //String imgPath = addr;
            String imgPath = path.getAbsolutePath()+"/static"+addr;
            BufferedImage image = ImageIO.read(new FileInputStream(imgPath));
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println();
            System.out.println("获取图片异常:java.awt.image.BufferedImage");
            System.out.println("请检查图片路径是否正确，或者该地址是否为一个图片");
        }
        return null;
    }
}
