package com.example.common.utils;

import com.example.common.exception.BadRequestException;
import com.example.common.exception.InternalServerErrorException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Base64;

public class FileUtils {

    private final static Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    private FileUtils() {
    }

    public static String getBas64String(byte[] byteFile) throws IOException {
        return BASE64_ENCODER.encodeToString(byteFile);
    }

    public static void writeFile(String directory, String fileName, String fileExtension, String base64Content)
        throws IOException {
        var filePath = Paths.get(directory);
        var baseDecoder = Base64.getDecoder();
        filePath = filePath.resolve(fileName + fileExtension);
        Files.write(filePath, baseDecoder.decode(base64Content));
    }

    public static byte[] readFileWithMapMemory(Path filePath) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            // Map the file to memory
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                fileChannel.size());
            // Read the file contents from the memory-mapped buffer
            byte[] fileByteContent = new byte[(int) fileChannel.size()];
            mappedByteBuffer.get(fileByteContent);
            return fileByteContent;
        }
    }

    public static void copyFile(
        String srcDirectory,
        String destDirectory,
        String file, String notFoundReturnCode) {
        var srcFilePath = Paths.get(srcDirectory, file);
        var trgFilePath = Paths.get(destDirectory, file);
        //check file
        if (!Files.exists(srcFilePath)) {
            throw new BadRequestException("resource-not-found");
        }
        try {
            Files.copy(srcFilePath, trgFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    public static void checkFileExist(Path filePath) throws FileNotFoundException {
        if (!Files.exists(filePath) && !Files.isRegularFile(filePath)) {
            throw new FileNotFoundException(String.format("file: %s is not exist", filePath.toAbsolutePath()));
        }
    }
}
