package com.YammyEater.demo.service.upload;

import java.util.List;
import java.util.UUID;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

/*
서로 다른 여러 서버에 자원을 업로드하는 방법을 추상화하는 인터페이스
자원을 KEY로 관리한다.
 */
public interface ResourceUploadService {
    String uploadResource(MultipartFile resource);
    void deleteResource(String key);
    void deleteResources(List<String> keys);
    String getURLFromKey(String key);
    default String createFileName(String uploadedName) {
        String ext = uploadedName.substring(uploadedName.lastIndexOf("."));
        return UUID.randomUUID().toString() + ext;
    }
    @Async
    default void deleteResourceAsync(String key) {
        deleteResource(key);
    }
    @Async
    default void deleteResourcesAsync(List<String> keys) {deleteResources(keys);}
}
