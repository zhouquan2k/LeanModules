package io.leanddd.module.file.local.model;

import io.leanddd.component.common.Util;
import io.leanddd.module.file.api.FileManagerSpi;
import io.leanddd.module.file.api.FileMeta;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RequiredArgsConstructor
public class FileManagerLocalFileSystem implements FileManagerSpi {

    @Value("${app.file.local.basePath}")
    private String fileRootPath;
    private final FileRepository repository;
    private final ConvertFile convert = Mappers.getMapper(ConvertFile.class);

    private Path resolve(FileMeta file) {
        Path uploadPath = Paths.get(fileRootPath + "/" + file.getPath());
        return uploadPath.resolve(String.format("f%s_%s", file.getId(), file.getFileName()));
    }

    @Override
    public FileMeta getMeta(String id) {
        return repository.findById(id).orElseThrow();
    }

    @Override
    public io.leanddd.module.file.api.File upload(MultipartFile file, String path) {
        try {
            Path uploadPath = Paths.get(fileRootPath);

            if (Util.isNotEmpty(path)) {
                uploadPath = uploadPath.resolve(path);
            }

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            File fileDO = File.builder()
                    .fileName(file.getOriginalFilename())
                    .path(path)
                    .size(file.getSize())
                    .mimeType(file.getContentType())
                    .build();
            fileDO = repository.save(fileDO);
            var filePath = resolve(fileDO);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return convert.doToVo(fileDO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stream(FileMeta meta, HttpServletResponse response) {

        Path filePath = resolve(meta);
        try (BufferedInputStream inStream = new BufferedInputStream(Files.newInputStream(filePath));
             BufferedOutputStream outStream = new BufferedOutputStream(response.getOutputStream())) {

            byte[] buffer = new byte[10240];
            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
                outStream.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error sending file", e);
        }
    }

    @Override
    public void delete(String id) {
        File file = repository.findById(id).orElseThrow();
        Path filePath = resolve(file);
        try {
            Files.delete(filePath);
            repository.delete(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
