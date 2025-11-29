package io.leanddd.module.file.model;

import io.leanddd.component.common.Util;
import io.leanddd.component.meta.Command;
import io.leanddd.component.meta.Service;
import io.leanddd.component.meta.Service.Type;
import io.leanddd.module.file.api.FileManagerSpi;
import io.leanddd.module.file.api.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Named
@Service(type = Type.Command, name = "file")
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private final FileManagerSpi fileManager;
    private static final long DEFAULT_CACHE_SECONDS = 30L * 24 * 60 * 60;

    @Override
    @Command(logParam = false)
    public io.leanddd.module.file.api.File uploadFile(MultipartFile file, String path) {
        
        Util.check(!file.isEmpty(), "Cannot upload empty file");

        return fileManager.upload(file, path);
    }

    @Override
    public void viewFile(String id, HttpServletRequest request, HttpServletResponse response) {
        var fileMeta = fileManager.getMeta(id);
        response.setContentType(fileMeta.getMimeType());
        applyCacheHeaders(response);
        fileManager.stream(fileMeta, request, response);
    }

    @Override
    public void viewFileEx(String id, String filename, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        var fileMeta = fileManager.getMeta(id);
        response.setContentType(fileMeta.getMimeType());
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        applyCacheHeaders(response);
        fileManager.stream(fileMeta, request, response);
    }

    @Override
    @Command(logParam = false)
    public void downloadFile(String id, HttpServletRequest request, HttpServletResponse response) {
        try {
            var fileMeta = fileManager.getMeta(id);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileMeta.getFileName(), "UTF-8"));
            applyCacheHeaders(response);
            fileManager.stream(fileMeta, request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteFile(String id) {
        fileManager.delete(id);
    }

    private void applyCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "public, max-age=" + DEFAULT_CACHE_SECONDS);
        response.setHeader("Expires", DateTimeFormatter.RFC_1123_DATE_TIME
                .format(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(DEFAULT_CACHE_SECONDS)));
    }
}
