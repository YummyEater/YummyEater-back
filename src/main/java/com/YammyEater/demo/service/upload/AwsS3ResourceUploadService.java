package com.YammyEater.demo.service.upload;

import com.YammyEater.demo.constant.error.ErrorCode;
import com.YammyEater.demo.domain.upload.TempResource;
import com.YammyEater.demo.exception.GeneralException;
import com.YammyEater.demo.repository.upload.TempResourceRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsS3ResourceUploadService implements ResourceUploadService {

    private final AmazonS3 amazonS3;
    private final TempResourceRepository tempResourceRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Override
    @Transactional
    public String uploadResource(MultipartFile resource) {
        //s3에서 사용될 key 생성
        String key = createFileName(resource.getOriginalFilename());
        //임시 자원 목록에 추가
        tempResourceRepository.save(
                TempResource.builder()
                        .key(key)
                        .uploadTime(LocalDateTime.now())
                        .build()
        );
        //s3에 업로드 전에 db에 올림으로써 트랜젝션이 실패했는데 업로드 되는 상황을 최대한 배제
        //TransactionSynchronizationManager의 afterRollback을 이용해서 자원을 삭제하는 방법도 좋아 보임
        tempResourceRepository.flush();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(resource.getSize());
        metadata.setContentType(resource.getContentType());
        try {
            amazonS3.putObject(bucketName, key, resource.getInputStream(), metadata);
        } catch (IOException e) {
            throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return key;
    }

    @Override
    public String getURLFromKey(String key) {
        return amazonS3.getUrl(bucketName, key).toString();
    }

    @Override
    public void deleteResource(String key) {
        amazonS3.deleteObject(bucketName, key);
    }

    @Override
    public void deleteResources(List<String> keys) {
        for(String key : keys) {
            amazonS3.deleteObject(bucketName, key);
        }
    }
}
