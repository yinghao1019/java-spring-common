package com.example.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Slf4j
public class FtpService {
    private final FtpRemoteFileTemplate ftpTemplate;

    public void downloadFile(String localDirectory, String fileName, String remoteDirectory) {
        ftpTemplate.get(remoteDirectory + fileName, inputStream -> {
            try {
                Path path = Paths.get(localDirectory + fileName);
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void uploadStringFile(String subDirectory, String remoteFileName, String fileContent) {
        uploadFile(subDirectory, remoteFileName, fileContent.getBytes(StandardCharsets.UTF_8));
    }

    public void uploadFile(String subDirectory, String remoteFileName, byte[] fileContent) {
        try (InputStream inputStream = new ByteArrayInputStream(fileContent)) {
            Message<InputStream> message = MessageBuilder.withPayload(inputStream)
                .setHeader(FileHeaders.FILENAME, remoteFileName).build();
            ftpTemplate.send(message, subDirectory);
        } catch (IOException e) {
            log.error("Upload file error,sourceFullName[{}], uploadPath[{}], {}", remoteFileName, subDirectory,
                e.getMessage());
        }
    }

    public void uploadFile(InputStream inputStream, String remotePath) throws IOException {
        ftpTemplate.execute(session -> {
            session.write(inputStream, remotePath);
            return null;
        });
    }

    public void deleteFile(String fileDirectory, String fileName) {
        ftpTemplate.execute(session -> {
            session.remove(fileDirectory + fileName);
            return null;
        });
    }

    public void renameFile(String fileDirectory, String oldFileName, String newFileName) {
        ftpTemplate.execute(session -> {
            session.rename(fileDirectory + oldFileName, fileDirectory + newFileName);
            return null;
        });
    }
}
