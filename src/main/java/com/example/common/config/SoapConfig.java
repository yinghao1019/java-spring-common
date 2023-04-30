package com.example.common.config;

import com.example.common.interceptor.SoapLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

@Configuration
public class SoapConfig {
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("com.example.common.model.wsdl");
        return marshaller;
    }

    @Bean
    public WebServiceMessageSender webServiceMessageSender() {
        // milliseconds
        HttpComponentsMessageSender sender = new HttpComponentsMessageSender();
        sender.setConnectionTimeout(5 * 1000);
        sender.setReadTimeout(5 * 1000);
        return sender;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller, WebServiceMessageSender sender) {
        var webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMarshaller(marshaller);
        webServiceTemplate.setUnmarshaller(marshaller);
        webServiceTemplate.setMessageSender(sender);

        ClientInterceptor[] clientInterceptors = { new SoapLoggingInterceptor() };
        webServiceTemplate.setInterceptors(clientInterceptors);
        return webServiceTemplate;
    }

}
