package com.example.common.config.aspect;

import com.example.common.exception.InternalServerErrorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;

@Component
@Order(1)
@Aspect
@RequiredArgsConstructor
@Slf4j
public class WebRequestLogAspect {
    private static final String WEB_REQUEST_INFO_LOG = "Web Request Info Log";
    private final ObjectMapper objectMapper;

    @Before("execution(* com.example.common.controller..* .*(..))")
    private void logWebRequestInfo(JoinPoint joinPoint) {
        MDC.put("requestBody", this.getRequestBody(joinPoint));
        log.info(WEB_REQUEST_INFO_LOG); // 紀錄所有 request 資訊
    }

    private String getRequestBody(JoinPoint joinPoint) {
        Annotation[][] paramAnnotations = ((MethodSignature) joinPoint.getSignature()).getMethod()
            .getParameterAnnotations();
        int index = -1;
        String requestBody = null;
        for (Annotation[] annotations : paramAnnotations) {
            index++;
            for (Annotation annotation : annotations) {
                if (!(annotation instanceof RequestBody)) {
                    continue;
                }
                try {
                    requestBody = this.objectMapper.writeValueAsString(joinPoint.getArgs()[index]);
                } catch (JsonProcessingException e) {
                    throw new InternalServerErrorException("Cannot transfer Object to String by objectMapper");
                }
            }
        }
        return requestBody;
    }
}
