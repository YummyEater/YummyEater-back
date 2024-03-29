package com.YammyEater.demo.domain.food;

import com.YammyEater.demo.constant.food.FoodType;
import com.YammyEater.demo.domain.BaseTimeEntity;
import com.YammyEater.demo.domain.user.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name="FOOD")
public class Food extends BaseTimeEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "FOOD_ID")
    private Long id;

    //음식 이름
    @Setter
    @Column(name = "NAME")
    private String name;

    //소개글 제목
    @Setter
    @Column(name = "TITLE")
    private String title;

    //리뷰 평균 평점
    @Setter
    @Column(name = "RATING")
    private float rating;

    //음식 종류(레시피 = RECIPE, 완제품 = PRODUCT, 배달 = DELIVERY)
    @Setter
    @Column(name = "TYPE")
    @Enumerated(value = EnumType.STRING)
    private FoodType type;

    //몇인분인지 정보
    @Setter
    @Column(name = "SERVINGS")
    private Integer servings;

    //중량 정보(완제품 등을 위함)
    @Setter
    @Column(name = "AMOUNT")
    private Float amount;

    //음식 재료 설명(닭, 배추 등)
    @Setter
    @Column(name = "INGREDIENT")
    private String ingredient;

    //가격
    @Setter
    @Column(name = "PRICE")
    private Long price;

    //회사(배달음식이나 완제품의 경우 회사명 필요)
    @Setter
    @Column(name = "MAKER")
    private String maker;

    //대표이미지 경로
    @Setter
    @Column(name = "IMG_URL")
    private String imgUrl;

    @Setter
    @Column(name = "VIEWS")
    private Integer views;

    //연결된 카테고리들
    @OneToMany(mappedBy = "food", fetch = FetchType.LAZY)
    private Set<FoodCategory> categories = new HashSet<>();

    //연결된 태그들
    @OneToMany(mappedBy = "food", fetch = FetchType.LAZY)
    private Set<FoodTag> tags = new HashSet<>();

    @OneToMany(mappedBy = "food", fetch = FetchType.LAZY)
    private List<FoodResource> foodResources = new ArrayList<>();

    //작성자
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    //영양소
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NUTRIENT_ID")
    private Nutrient nutrient;

    //기사
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ARTICLE_ID")
    private Article article;

    //리뷰 별점 통계
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOOD_REVIEW_RATING_COUNT_ID")
    private FoodReviewRatingCount foodReviewRatingCount;


    @Builder
    public Food(
            String title,
            float rating,
            FoodType type,
            Integer servings,
            Float amount,
            String ingredient,
            Long price,
            String maker,
            String imgUrl,
            Integer views,
            User user,
            Nutrient nutrient,
            Article article,
            FoodReviewRatingCount foodReviewRatingCount
    ) {
        this.title = title;
        this.rating = rating;
        this.type = type;
        this.servings = servings;
        this.amount = amount;
        this.ingredient = ingredient;
        this.price = price;
        this.maker = maker;
        this.imgUrl = imgUrl;
        this.views = views;
        this.user = user;
        this.nutrient = nutrient;
        this.article = article;
        this.foodReviewRatingCount = foodReviewRatingCount;
    }
}
