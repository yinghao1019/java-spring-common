package com.example.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.function.Predicate;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    //客製化swagger 設定
    @Bean
    public Docket docket() {
        /**
         * apis():指定掃描的介面
         *  RequestHandlerSelectors:設定要掃描介面的方式
         *       basePackage:指定要掃描的包
         *       any:掃面全部
         *       none:不掃描
         *       withClassAnnotation:掃描類上的註解(引數是類上註解的class物件)
         *       withMethodAnnotation:掃描方法上的註解(引數是方法上的註解的class物件)
         * paths():過濾路徑
         *  PathSelectors:設定過濾的路徑
         *      any:過濾全部路徑
         *      none:不過濾路徑
         *      ant:過濾指定路徑:按照按照Spring的AntPathMatcher提供的match方法進行匹配
         *      regex:過濾指定路徑:按照String的matches方法進行匹配
         */

        return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo())
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.example.common.controller"))
            .paths(PathSelectors.any())
            //排除swagger 範例的controller
            .paths(Predicate.not(PathSelectors.regex("/error.*")))
            .build();
    }

    //設定Swagger資訊
    private ApiInfo apiInfo() {
        return new ApiInfo(
            "Java spring Utils example",
            "this is java spring framwork utils tool collections",
            "1.0",
            "",
            //作者資訊
            new Contact("Howard Hung", "", "z112517z@gmail.com"),
            "",
            "",
            Collections.EMPTY_LIST);
    }
}