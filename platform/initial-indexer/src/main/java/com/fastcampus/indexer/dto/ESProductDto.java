package com.fastcampus.indexer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ESProductDto {
    Long id;
    String asin;
    String title;
    String imgUrl;
    String productUrl;
    float stars;
    int reviews;
    BigDecimal price;
    BigDecimal listPrice;
    int categoryId;
    @JsonProperty("is_best_seller")
    boolean isBestSeller;
    int boughtInLastMonth;
    @JsonProperty("is_recommend_seller")
    boolean isRecommendSeller;
    Date createdAt;
    Date updatedAt;
}
