package com.YammyEater.demo.dto.food;

import com.YammyEater.demo.constant.food.FoodType;

public record FoodConditionalRequest(
        FoodType type,
        String title,
        String[] nutrient,
        String[] ingredients,
        String[] categories,
        String[] tags,
        Long userId,
        String userName
) {
}
