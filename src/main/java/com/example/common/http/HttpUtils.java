package com.example.common.http;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class HttpUtils {
    public static String getIpAddress(HttpServletRequest httpServletRequest) {
        String ipAddress = httpServletRequest.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isBlank(ipAddress)) {
            ipAddress = httpServletRequest.getRemoteAddr();
        }
        // 使用 localhost 會接收到 0:0:0:0:0:0:0:1
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }
        return ipAddress;
    }
}
