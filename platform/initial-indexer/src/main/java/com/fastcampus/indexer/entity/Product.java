package com.fastcampus.indexer.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
@ToString
@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @javax.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    boolean isRecommendSeller;
    @CreationTimestamp
    Timestamp createdAt;
    @UpdateTimestamp
    Timestamp updatedAt;
}
