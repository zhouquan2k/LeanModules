package io.leanddd.module.file.model;

import io.leanddd.component.common.Util;
import io.leanddd.component.meta.Command;
import io.leanddd.component.meta.Service;
import io.leanddd.component.meta.Service.Type;
import io.leanddd.module.file.api.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

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

    @Override
    @Command(logParam = false)
    public void downloadFile(String id, HttpServletResponse response) {
        fileManager.download(id, response);
    }

    @Override
    public void deleteFile(String id) {
        fileManager.delete(id);
    }
}
