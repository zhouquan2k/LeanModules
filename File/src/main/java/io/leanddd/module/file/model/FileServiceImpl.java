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
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

@Named
@Service(type = Type.Command, name = "file")
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private final FileManagerSpi fileManager;

    @Override
    @Command(logParam = false)
    public io.leanddd.module.file.api.File uploadFile(MultipartFile file, String path) {
        
        Util.check(!file.isEmpty(), "Cannot upload empty file");

        return fileManager.upload(file, path);
    }

    public void viewFile(String id, HttpServletResponse response) {
        var fileMeta = fileManager.getMeta(id);
        response.setContentType(fileMeta.getMimeType());
        fileManager.stream(fileMeta, response);
    }

    @Override
    @Command(logParam = false)
    public void downloadFile(String id, HttpServletResponse response) {
        try {
            var fileMeta = fileManager.getMeta(id);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileMeta.getFileName(), "UTF-8"));
            fileManager.stream(fileMeta, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteFile(String id) {
        fileManager.delete(id);
    }
}
