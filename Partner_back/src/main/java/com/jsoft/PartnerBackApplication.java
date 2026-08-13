package com.jsoft;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.jsoft.mapper")
public class PartnerBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerBackApplication.class, args);
    }

}
