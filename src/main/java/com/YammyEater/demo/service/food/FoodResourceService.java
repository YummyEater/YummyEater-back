package com.YammyEater.demo.service.food;

import com.YammyEater.demo.domain.food.FoodResource;
import java.util.List;

public interface FoodResourceService {
    //주어진 key의 자원을 삭제
    void deleteResource(String resourceKey);
    //주어진 key의 자원들을 삭제
    void deleteResources(List<String> resourceKeys);

    //주어진 key의 자원을 FoodResource로 등록
    void registerResource(Long foodId, String tempResourceKey);
    //주어진 key의 자원들을 FoodResource로 등록
    void registerResources(Long foodId, List<String> tempResourceKeys);

    //주어진 key들로 모든 자원을 대체
    void setResources(Long foodId, List<String> keys);
}
