package com.YammyEater.demo.service.upload;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class TransactionResourceUploadServiceImpl implements TransactionResourceUploadService {

    private final ResourceUploadService resourceUploadService;

    @Override
    @Transactional
    public void deleteResourceAfterCommit(String key) {
        //기존 트랜젝션 커밋 이후에 자원을 삭제
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        resourceUploadService.deleteResource(key);
                    }
                }
        );
    }

    @Override
    @Transactional
    public void deleteResourceAsyncAfterCommit(String key) {
        //기존 트랜젝션 커밋 이후에 비동기로 자원을 삭제
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        resourceUploadService.deleteResourceAsync(key);
                    }
                }
        );
    }

    @Override
    @Transactional
    public void deleteResourcesAsyncAfterCommit(List<String> keys) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        resourceUploadService.deleteResourcesAsync(keys);
                    }
                }
        );
    }
}
