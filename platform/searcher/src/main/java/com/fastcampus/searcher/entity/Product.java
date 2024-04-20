package com.fastcampus.searcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import java.sql.Timestamp;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("asin")
    private String asin;

    @JsonProperty("title")
    private String title;
    @JsonProperty("img_url")
    private String img_url;
    @JsonProperty("product_url")
    private String product_url;
    @JsonProperty("stars")
    private double stars;
    @JsonProperty("reviews")
    private int reviews;
    @JsonProperty("price")
    private double price;
    @JsonProperty("list_price")
    private double list_price;
    @JsonProperty("category_id")
    private long category_id;
    @JsonProperty("bought_in_last_month")
    private int bought_in_last_month;
    @JsonProperty("created_at")
    private Timestamp created_at;
    @JsonProperty("updated_at")
    private Timestamp updated_at;
    @JsonProperty("is_best_seller")
    private boolean is_best_seller;
    @JsonProperty("is_recommend_seller")
    private boolean is_recommend_seller;
}
