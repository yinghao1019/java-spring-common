package com.example.common.config;

import lombok.Data;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;

@Configuration
public class FtpConfig {

    @Value("${spring.ftp.remote-directory}")
    private String remoteDirectory;

    @Bean
    public SessionFactory<FTPFile> ftpSessionFactory(FtpSessionFactoryConfig config) {
        DefaultFtpSessionFactory factory = new DefaultFtpSessionFactory();
        factory.setHost(config.host);
        factory.setPort(config.port);
        factory.setUsername(config.username);
        factory.setPassword(config.password);
        factory.setControlEncoding("UTF-8");
        factory.setFileType(FTP.ASCII_FILE_TYPE);

        CachingSessionFactory<FTPFile> cachingSessionFactory = new CachingSessionFactory<>(factory);
        cachingSessionFactory.setPoolSize(config.poolSize);

        return cachingSessionFactory;
    }

    @Bean
    public FtpRemoteFileTemplate ftpTemplate(SessionFactory<FTPFile> sessionFactory) {
        FtpRemoteFileTemplate ftpRemoteFileTemplate = new FtpRemoteFileTemplate(sessionFactory);
        ftpRemoteFileTemplate.setRemoteDirectoryExpression(new LiteralExpression(remoteDirectory));
        return ftpRemoteFileTemplate;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.ftp")
    public FtpSessionFactoryConfig ftpSessionFactoryProperties() {
        return new FtpSessionFactoryConfig();
    }

    @Data
    static class FtpSessionFactoryConfig {
        private String host;
        private Integer port;
        private String username;
        private String password;
        private Integer poolSize;
        private Integer dataTimeout;
    }
}