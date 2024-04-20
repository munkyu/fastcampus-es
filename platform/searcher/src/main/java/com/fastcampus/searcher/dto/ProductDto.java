package com.fastcampus.searcher.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private String asin;
    private String title;
    private String img_url;
    private String product_url;
    private double stars;
    private int reviews;
    private double price;
    private double list_price;
    private String created_at;
    private String updated_at;
    @JsonIgnore
    private Long category_id;

    private String category_name;

    private double score;
}
