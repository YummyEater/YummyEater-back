package com.YammyEater.demo.dto.food;

import com.YammyEater.demo.constant.food.FoodType;
import com.YammyEater.demo.domain.food.Food;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record FoodSimpleResponse(
        Long id,
        FoodType type,
        String title,

        Long userId,
        String userName,

        String imgUrl,

        float rating,

        Long price,
        String maker,

        List<String> categories,
        List<String> tags,

        Integer views,

        String createdAt,
        String lastModifiedAt
) {
    public static FoodSimpleResponse of(Food food) {
        return new FoodSimpleResponse(
                food.getId(),
                food.getType(),
                food.getTitle(),
                food.getUser().getId(),
                food.getUser().getUsername(),
                food.getImgUrl(),
                food.getRating(),
                food.getPrice(),
                food.getMaker(),
                food.getCategories().stream().map(x -> x.getCategory().getName()).toList(),
                food.getTags().stream().map(x -> x.getTag()).toList(),
                food.getViews(),
                food.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME),
                food.getLastModifiedAt().format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }
}
