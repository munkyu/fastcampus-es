package com.fastcampus.searcher.repository;

import com.fastcampus.searcher.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
