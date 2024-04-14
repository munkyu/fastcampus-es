package com.fastcampus.indexer.domain;

import lombok.*;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Products {
    @javax.persistence.Id
    @Id
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
    boolean isBestSeller;
    int boughtInLastMonth;
    Timestamp createdAt;
    Timestamp updatedAt;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
