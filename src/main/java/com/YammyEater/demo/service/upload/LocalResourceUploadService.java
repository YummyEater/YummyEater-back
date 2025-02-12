package com.YammyEater.demo.service.upload;

import com.YammyEater.demo.constant.error.ErrorCode;
import com.YammyEater.demo.domain.upload.TempResource;
import com.YammyEater.demo.exception.upload.ResourceUploadException;
import com.YammyEater.demo.repository.upload.TempResourceRepository;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/*
로컬서버의 파일 시스템에 업로드하는 서비스
 */
@RequiredArgsConstructor
public class LocalResourceUploadService implements ResourceUploadService {

    private final String UPLOAD_ROOT;

    private final String RESOURCE_HOST;

    private final String URL_PATH;

    private final TempResourceRepository tempResourceRepository;

    @Override
    public String uploadResource(MultipartFile resource) {
        String filename = createFileName(resource.getOriginalFilename());
        File saveTo = new File(getRealPath(filename));
        try {
            resource.transferTo(saveTo);
        }
        catch (IOException e) {
            throw new ResourceUploadException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        tempResourceRepository.save(new TempResource(filename, LocalDateTime.now()));
        return filename;
    }

    @Override
    public void deleteResource(String key) {
        File deleteFile = new File(getRealPath(key));
        deleteFile.delete();
    }

    @Override
    public void deleteResources(List<String> keys) {
        for(String key : keys) {
            deleteResource(key);
        }
    }

    @Override
    public String getURLFromKey(String key) {
        return RESOURCE_HOST + URL_PATH + key;
    }

    private String getRealPath(String resourcePath) {
        return UPLOAD_ROOT + resourcePath;
    }
}
