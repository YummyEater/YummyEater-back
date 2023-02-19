package com.YammyEater.demo.domain;

import com.YammyEater.demo.constant.FoodType;
import java.util.ArrayList;
import java.util.List;
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
@Builder
@Getter
@Table(name="FOOD")
public class Food {
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

    //연결된 태그들
    @OneToMany(mappedBy = "food", fetch = FetchType.LAZY)
    private List<FoodTag> tags = new ArrayList<>();

    //작성자
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User writer;

    //영양소
    @Setter
    @OneToOne(mappedBy = "food", fetch = FetchType.LAZY)
    private Nutrient nutrient;

    //기사
    @Setter
    @OneToOne(mappedBy = "food", fetch = FetchType.LAZY)
    private Article article;

    //리뷰 별점 통계
    @Setter
    @OneToOne(mappedBy = "food", fetch = FetchType.LAZY)
    private FoodReviewRatingCount foodReviewRatingCount;

}