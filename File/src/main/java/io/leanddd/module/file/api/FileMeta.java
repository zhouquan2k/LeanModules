package io.leanddd.module.file.api;

public interface FileMeta {
    String getId(); //unique id
    String getKey();
    String getFileName();
    String getPath();
    Long getSize();
    String getMimeType();
}