package com.example.common.interceptor;

import com.example.common.constant.HeaderParam;
import com.example.common.constant.LogParam;
import com.example.common.http.HttpUtils;
import org.slf4j.MDC;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.handler.DispatcherServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class RequestLoggingInterceptor implements WebRequestInterceptor {
    // Request處理前先呼叫preHandle()
    @Override public void preHandle(WebRequest webRequest) throws Exception {
        MDC.put(LogParam.REQUEST_OID, UUID.randomUUID().toString());
        HttpServletRequest httpServletRequest = ((DispatcherServletWebRequest) webRequest).getRequest();
        String ipAddress = HttpUtils.getIpAddress(httpServletRequest);
        String acceptLanguage = httpServletRequest.getHeader(HeaderParam.ACCEPT_LANGUAGE);
        if (acceptLanguage != null) {
            MDC.put(HeaderParam.ACCEPT_LANGUAGE, acceptLanguage);
        }
        MDC.put("ip", ipAddress);
        MDC.put("req.remoteHost", httpServletRequest.getRemoteHost());
        MDC.put("req.requestURI", httpServletRequest.getRequestURI());
        StringBuffer requestURL = httpServletRequest.getRequestURL();
        if (requestURL != null) {
            MDC.put("req.requestURL", requestURL.toString());
        }
        MDC.put("req.method", httpServletRequest.getMethod());
        MDC.put("req.queryString", httpServletRequest.getQueryString());
        MDC.put("req.userAgent", httpServletRequest.getHeader("User-Agent"));
        MDC.put("req.xForwardedFor", httpServletRequest.getHeader("X-Forwarded-For"));

        OffsetDateTime now = OffsetDateTime.now().toInstant().atOffset(ZoneOffset.of(""));
        String yearMonth = String.format("%s-%s", now.getYear(), String.format("%02d", now.getMonthValue()));
        MDC.put(LogParam.LOG_FILE_DISCRIMINATOR, yearMonth + "/User");
    }

    @Override public void postHandle(WebRequest request, ModelMap model) throws Exception {

    }

    @Override public void afterCompletion(WebRequest request, Exception ex) throws Exception {

    }
}
