package com.fastcampus.indexer.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    String asin;
    String title;
    String imgUrl;
    String productUrl;
    float stars;
    int reviews;
    BigDecimal price;
    BigDecimal listPrice;
    int categoryId;
    boolean isBestSeller;
    int boughtInLastMonth;
}
