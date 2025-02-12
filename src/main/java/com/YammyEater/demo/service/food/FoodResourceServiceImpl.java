package com.YammyEater.demo.service.food;

import com.YammyEater.demo.domain.food.Food;
import com.YammyEater.demo.domain.food.FoodResource;
import com.YammyEater.demo.exception.food.FoodNotExistException;
import com.YammyEater.demo.repository.food.FoodRepository;
import com.YammyEater.demo.repository.food.FoodResourceRepository;
import com.YammyEater.demo.repository.upload.TempResourceRepository;
import com.YammyEater.demo.service.upload.TransactionResourceUploadService;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FoodResourceServiceImpl implements FoodResourceService {

    private final TransactionResourceUploadService transactionResourceUploadService;
    private final FoodRepository foodRepository;
    private final FoodResourceRepository foodResourceRepository;
    private final TempResourceRepository tempResourceRepository;

    @Override
    @Transactional
    public void deleteResource(String resourceKey) {
        transactionResourceUploadService.deleteResourceAsyncAfterCommit(resourceKey);
        foodResourceRepository.deleteById(resourceKey);
    }

    @Override
    @Transactional
    public void deleteResources(List<String> keys) {
        transactionResourceUploadService.deleteResourcesAsyncAfterCommit(keys);
        foodResourceRepository.deleteAllByIdInBatch(keys);
    }

    @Override
    @Transactional
    public void registerResource(Long foodId, String tempResourceKey) {
        Food food = foodRepository.findById(foodId).orElseThrow(FoodNotExistException::new);
        //임시 업로드 자원 목록에 존재해야함
        if(tempResourceKey == null || !tempResourceRepository.existsById(tempResourceKey)) {
            return;
        }
        foodResourceRepository.save(
                FoodResource.builder()
                        .food(food)
                        .key(tempResourceKey)
                        .build()
        );
        tempResourceRepository.deleteById(tempResourceKey);
    }

    @Override
    @Transactional
    public void registerResources(Long foodId, List<String> tempResourceKeys) {
        Food food = foodRepository.findById(foodId).orElseThrow(FoodNotExistException::new);
        for(String key : tempResourceKeys) {
            //임시 업로드 자원 목록에 존재해야함
            if(key == null || !tempResourceRepository.existsById(key)) {
                return;
            }
            foodResourceRepository.save(
                    FoodResource.builder()
                            .food(food)
                            .key(key)
                            .build()
            );
        }
        tempResourceRepository.deleteAllByIdInBatch(tempResourceKeys);
    }

    @Override
    @Transactional
    public void setResources(Long foodId, List<String> keys) {
        Food food = foodRepository.findById(foodId).orElseThrow(FoodNotExistException::new);
        //기존에 등록되어 있던 FoodResource
        Set<FoodResource> originalFoodResources = new HashSet<>(food.getFoodResources());
        //새로 요청된 FoodResource
        Set<FoodResource> newFoodResources = new HashSet<>();
        for(String key : keys) {
            if(key == null) {
                continue;
            }
            newFoodResources.add(
                    new FoodResource(key, food)
            );
        }
        //삭제할 자원
        deleteResources(
                Sets.difference(originalFoodResources, newFoodResources).stream()
                        .map(FoodResource::getKey)
                        .collect(Collectors.toList())
        );
        //추가할 자원
        registerResources(
                foodId,
                Sets.difference(newFoodResources, originalFoodResources).stream()
                        .map(FoodResource::getKey)
                        .collect(Collectors.toList())
        );
    }
}
