package com.jsoft.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {

    /**
     *  创建Rest API
     * @return
     */
    @Bean(value = "defaultApi")
    public Docket defaultApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.jsoft.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     *  api信息
     * @return
     */
    public ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("伙伴匹配系统")//// 设置文档的标题
                .description("鱼皮用户中心接口文档")// 设置文档的描述->1.Overview
                .version("1.0")// 设置文档的版本信息
                .contact(new Contact("F4EN", "https://github.com/1298650702lc-ops/Partner_Match_System", "xxx"))
                .termsOfServiceUrl("https://github.com/1298650702lc-ops/Partner_Match_System")// 设置文档的License信息
                .build();
    }
}
