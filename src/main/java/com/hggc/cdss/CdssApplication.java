package com.hggc.cdss;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//指定数据访问接口包名
@MapperScan("com.hggc.cdss.execution.mapper")
public class CdssApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdssApplication.class, args);
    }

}
