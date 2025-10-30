package io.leanddd.module.file.s3;

import io.leanddd.module.file.api.FileManagerSpi;
import io.leanddd.module.file.api.FileMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT;
import static javax.servlet.http.HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE;

/**
 * S3文件管理器，基于S3兼容接口
 * 实现stream方法，其他方法由子类实现
 */
@Slf4j
// @RequiredArgsConstructor
public class S3FileManager implements FileManagerSpi {

    // @Value("${app.file.s3.bucket:default}")
    // protected String bucket;

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
    protected ResponseInputStream<GetObjectResponse> openS3ObjectStream(String path) {
        return openS3ObjectStream(path, null);
    }

    protected ResponseInputStream<GetObjectResponse> openS3ObjectStream(String path, String range) {
        var location = resolveLocation(path);
        GetObjectRequest.Builder requestBuilder = GetObjectRequest.builder()
                .bucket(location.bucket())
                .key(location.key());
        if (range != null) {
            requestBuilder.range(range);
        }

        return s3Client.getObject(requestBuilder.build());
    }

    /**
     * 实现stream方法，支持预览和下载
     * 通过设置不同的响应头来区分预览和下载行为
     */
    @Override
    public void stream(FileMeta meta, HttpServletRequest request, HttpServletResponse response) {
        var location = resolveLocation(meta.getPath());
        String key = meta.getKey();
        String rangeHeader = request != null ? request.getHeader("Range") : null;
        Long objectSize = resolveObjectSize(meta, location);

        response.setHeader("Accept-Ranges", "bytes");

        try {
            if (rangeHeader != null && !rangeHeader.isBlank() && objectSize != null) {
                ByteRange byteRange = parseRange(rangeHeader, objectSize);
                if (byteRange == null) {
                    response.setStatus(SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    response.setHeader("Content-Range", String.format("bytes */%d", objectSize));
                    return;
                }

                String s3Range = String.format("bytes=%d-%d", byteRange.start(), byteRange.end());
                try (ResponseInputStream<GetObjectResponse> s3Stream = openS3ObjectStream(meta.getPath(), s3Range);
                     BufferedInputStream inStream = new BufferedInputStream(s3Stream);
                     BufferedOutputStream outStream = new BufferedOutputStream(response.getOutputStream())) {

                    response.setStatus(SC_PARTIAL_CONTENT);
                    response.setHeader("Content-Range", String.format("bytes %d-%d/%d", byteRange.start(), byteRange.end(), objectSize));
                    response.setHeader("Content-Length", String.valueOf(byteRange.length()));

                    transferStream(inStream, outStream);
                    log.debug("Successfully streamed S3 range: bucket={}, key={}, range={}", location.bucket(), key, s3Range);
                }
                return;
            }

            try (ResponseInputStream<GetObjectResponse> s3Stream = openS3ObjectStream(meta.getPath());
                 BufferedInputStream inStream = new BufferedInputStream(s3Stream);
                 BufferedOutputStream outStream = new BufferedOutputStream(response.getOutputStream())) {

                Long s3ContentLength = s3Stream.response().contentLength();
                long contentLength = objectSize != null ? objectSize : (s3ContentLength != null ? s3ContentLength : -1L);
                if (contentLength >= 0) {
                    response.setHeader("Content-Length", String.valueOf(contentLength));
                }

                transferStream(inStream, outStream);
                log.debug("Successfully streamed S3 object: bucket={}, key={}", location.bucket(), key);
            }
        } catch (IOException e) {
            log.error("Error streaming S3 object: bucket={}, key={}", location.bucket(), key, e);
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
    protected void uploadToS3(String path, MultipartFile file) {
        var bucket = path.substring(0, path.indexOf('/'));
        var key = path.substring(path.indexOf('/') + 1);
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
    protected void deleteFromS3(String path) {
        var bucket = path.substring(0, path.indexOf('/'));
        var key = path.substring(path.indexOf('/') + 1);
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

    private Long resolveObjectSize(FileMeta meta, ObjectLocation location) {
        if (meta.getSize() != null) {
            return meta.getSize();
        }
        try {
            return s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(location.bucket())
                    .key(location.key())
                    .build()).contentLength();
        } catch (Exception e) {
            log.warn("Unable to resolve object size via HeadObject: bucket={}, key={}", location.bucket(), location.key(), e);
            return null;
        }
    }

    private ByteRange parseRange(String rangeHeader, long objectSize) {
        String header = rangeHeader.trim();
        if (!header.startsWith("bytes=")) {
            return null;
        }

        String spec = header.substring("bytes=".length()).trim();
        if (spec.isEmpty()) {
            return null;
        }

        String rangeSpec = spec.split(",", 2)[0].trim();
        int dashIndex = rangeSpec.indexOf('-');
        if (dashIndex < 0) {
            return null;
        }

        try {
            if (dashIndex == 0) {
                String suffixPart = rangeSpec.substring(1).trim();
                if (suffixPart.isEmpty()) {
                    return null;
                }
                long suffixLength = Long.parseLong(suffixPart);
                if (suffixLength <= 0) {
                    return null;
                }
                long length = Math.min(suffixLength, objectSize);
                long start = objectSize - length;
                long end = objectSize - 1;
                return new ByteRange(start, end);
            }

            String startPart = rangeSpec.substring(0, dashIndex).trim();
            String endPart = rangeSpec.substring(dashIndex + 1).trim();

            long start = Long.parseLong(startPart);
            if (start < 0 || start >= objectSize) {
                return null;
            }

            long end;
            if (endPart.isEmpty()) {
                end = objectSize - 1;
            } else {
                end = Long.parseLong(endPart);
                if (end < start) {
                    return null;
                }
                if (end >= objectSize) {
                    end = objectSize - 1;
                }
            }

            return new ByteRange(start, end);
        } catch (NumberFormatException ex) {
            log.debug("Invalid Range header encountered: {}", rangeHeader, ex);
            return null;
        }
    }

    private void transferStream(BufferedInputStream inStream, BufferedOutputStream outStream) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int length;
        while ((length = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, length);
        }
        outStream.flush();
    }

    private ObjectLocation resolveLocation(String path) {
        int delimiter = path.indexOf('/');
        if (delimiter < 0) {
            throw new IllegalArgumentException("Invalid S3 path format: " + path);
        }
        return new ObjectLocation(path.substring(0, delimiter), path.substring(delimiter + 1));
    }

    private static final class ObjectLocation {
        private final String bucket;
        private final String key;

        ObjectLocation(String bucket, String key) {
            this.bucket = bucket;
            this.key = key;
        }

        String bucket() {
            return bucket;
        }

        String key() {
            return key;
        }
    }

    private static final class ByteRange {
        private final long start;
        private final long end;

        ByteRange(long start, long end) {
            this.start = start;
            this.end = end;
        }

        long start() {
            return start;
        }

        long end() {
            return end;
        }

        long length() {
            return end - start + 1;
        }
    }
}
