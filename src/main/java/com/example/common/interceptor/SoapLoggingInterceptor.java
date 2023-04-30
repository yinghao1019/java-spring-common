package com.example.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class SoapLoggingInterceptor implements ClientInterceptor {
    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            messageContext.getRequest().writeTo(out);
            String outStr = out.toString(StandardCharsets.UTF_8);
            log.info("soap request message:\n{}", outStr);
        } catch (Exception e) {
            log.error("error...", e);
        }

        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            messageContext.getResponse().writeTo(out);
            String outStr = out.toString("UTF-8");
            log.info("== success response == \nmessage:{}", outStr);
        } catch (Exception e) {
            log.error("error...", e);
        }
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            messageContext.getResponse().writeTo(out);
            String outStr = out.toString("UTF-8");
            log.info("== fault == \n messageContext:{}", outStr);
        } catch (Exception e) {
            log.error("error...", e);
        }
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex)
        throws WebServiceClientException {
    }
}
