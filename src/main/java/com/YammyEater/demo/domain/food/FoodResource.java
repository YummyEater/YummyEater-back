package com.YammyEater.demo.domain.food;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "FOOD_RESOURCE")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodResource {
    @Id
    @Column(name="resource_key")
    private String key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="food_id")
    private Food food;

    @Override
    public boolean equals(Object o) {
        //레퍼런스가 같다면 무조건 동일
        if(this == o) return true;
        //비교 대상이 null이면 false
        //비교 대상이 FoodResource의 instance가 아니라면 false(프록시 대응)
        if(o == null || !(o instanceof FoodResource)) return false ;
        FoodResource foodResource = (FoodResource) o;
        //pk로 값 비교
        return this.key.equals(foodResource.getKey());
    }

    @Override
    public int hashCode() {
        if(key == null) {
            return 0;
        }
        return key.hashCode();
    }
}
