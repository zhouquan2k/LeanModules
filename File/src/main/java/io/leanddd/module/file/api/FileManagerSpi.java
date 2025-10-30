package io.leanddd.module.file.api;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FileManagerSpi {
    FileMeta getMeta(String id);

    File upload(MultipartFile file, String path);

    void stream(FileMeta meta, HttpServletRequest request, HttpServletResponse response);

    void delete(String id);
}
