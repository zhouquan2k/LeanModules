package io.leanddd.module.file.s3;

import io.leanddd.module.file.api.FileManagerSpi;
import io.leanddd.module.file.api.FileMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * S3文件管理器，基于S3兼容接口
 * 实现stream方法，其他方法由子类实现
 */
@Slf4j
// @RequiredArgsConstructor
public class S3FileManager implements FileManagerSpi {

    @Value("${app.file.s3.bucket:default}")
    protected String bucket;

    @Value("${app.file.s3.keyPrefix:}")
    protected String keyPrefix;

    @Value("${app.file.s3.bufferSize:10240}")
    protected int bufferSize;

    protected S3Client s3Client;

    public S3FileManager(S3Client s3Client) {
        this.s3Client = s3Client;
    }



    /**
     * 打开S3对象输入流
     */
    protected ResponseInputStream<GetObjectResponse> openS3ObjectStream(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        
        return s3Client.getObject(request);
    }

    /**
     * 实现stream方法，支持预览和下载
     * 通过设置不同的响应头来区分预览和下载行为
     */
    @Override
    public void stream(FileMeta meta, HttpServletResponse response) {
        String key = meta.getKey();
        
        try (ResponseInputStream<GetObjectResponse> s3Stream = openS3ObjectStream(key);
             BufferedInputStream inStream = new BufferedInputStream(s3Stream);
             BufferedOutputStream outStream = new BufferedOutputStream(response.getOutputStream())) {

            byte[] buffer = new byte[bufferSize];
            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
                outStream.flush();
            }
            
            log.debug("Successfully streamed S3 object: bucket={}, key={}", bucket, key);
            
        } catch (IOException e) {
            log.error("Error streaming S3 object: bucket={}, key={}", bucket, key, e);
            throw new RuntimeException("Error streaming file from S3", e);
        }
    }

    @Override
    public FileMeta getMeta(String id) {
        // TODO: 由子类实现，可以从数据库或其他元数据存储获取
        // 这里需要根据实际业务需求实现
        throw new UnsupportedOperationException("getMeta method must be implemented by subclass");
    }

    @Override
    public io.leanddd.module.file.api.File upload(MultipartFile file, String path) {
        // TODO: 由子类实现，需要：
        // 1. 生成文件ID
        // 2. 保存元数据到数据库
        // 3. 上传文件到S3
        // 4. 返回File对象
        throw new UnsupportedOperationException("upload method must be implemented by subclass");
    }

    @Override
    public void delete(String id) {
        // TODO: 由子类实现，需要：
        // 1. 获取文件元数据
        // 2. 从S3删除文件
        // 3. 从数据库删除元数据
        throw new UnsupportedOperationException("delete method must be implemented by subclass");
    }

    /**
     * 上传文件到S3（供子类调用）
     */
    protected void uploadToS3(String key, MultipartFile file) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            log.debug("Successfully uploaded file to S3: bucket={}, key={}", bucket, key);
            
        } catch (Exception e) {
            log.error("Error uploading file to S3: bucket={}, key={}", bucket, key, e);
            throw new RuntimeException("Error uploading file to S3", e);
        }
    }

    /**
     * 从S3删除文件（供子类调用）
     */
    protected void deleteFromS3(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            
            log.debug("Successfully deleted file from S3: bucket={}, key={}", bucket, key);
            
        } catch (Exception e) {
            log.error("Error deleting file from S3: bucket={}, key={}", bucket, key, e);
            throw new RuntimeException("Error deleting file from S3", e);
        }
    }
}
