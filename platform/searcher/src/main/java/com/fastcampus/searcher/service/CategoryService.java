package com.fastcampus.searcher.service;

import com.fastcampus.searcher.entity.Category;
import com.fastcampus.searcher.repository.CategoryRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(cacheNames = "categories", key = "#id")
    @Scheduled(fixedRate = 43200000) // 12 hours
    public Category getCategory(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }
}
