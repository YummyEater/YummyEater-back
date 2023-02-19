package com.YammyEater.demo.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "FOOD_REVIEW_RATING_COUNT")
public class FoodReviewRatingCount {
    @Id
    @Column(name = "FOOD_ID")
    private Long id;

    @Column(name = "RATE1")
    private Long rate1;

    @Column(name = "RATE2")
    private Long rate2;

    @Column(name = "RATE3")
    private Long rate3;

    @Column(name = "RATE4")
    private Long rate4;

    @Column(name = "RATE5")
    private Long rate5;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOOD_ID")
    private Food food;

    @Builder
    public FoodReviewRatingCount(Long id, Long rate1, Long rate2, Long rate3, Long rate4, Long rate5) {
        this.id = id;
        this.rate1 = rate1;
        this.rate2 = rate2;
        this.rate3 = rate3;
        this.rate4 = rate4;
        this.rate5 = rate5;
    }

    public FoodReviewRatingCount() {
    }
}
