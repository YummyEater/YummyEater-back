package com.YammyEater.demo.service.upload;

import java.util.List;

/*
 * 트랜젝션과 관련한 자원 업로드를 연계한 다양한 기능 지원
 */
public interface TransactionResourceUploadService {
    //현재 진행중인 트랜젝션 커밋 이후에 자원을 삭제
    void deleteResourceAfterCommit(String key);
    //현재 진행중인 트랜젝션 커밋 이후에 자원을 비동기로 삭제
    void deleteResourceAsyncAfterCommit(String key);
    void deleteResourcesAsyncAfterCommit(List<String> keys);
}