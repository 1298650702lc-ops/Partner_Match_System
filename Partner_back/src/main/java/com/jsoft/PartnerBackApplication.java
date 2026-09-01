package com.jsoft;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.jsoft.mapper")
@EnableScheduling //定时任务
public class PartnerBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerBackApplication.class, args);
    }

}
