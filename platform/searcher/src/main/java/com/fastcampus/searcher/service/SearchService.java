package com.fastcampus.searcher.service;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fastcampus.searcher.dto.ProductDto;
import com.fastcampus.searcher.entity.Product;
import com.fastcampus.searcher.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {
    private final ElasticsearchRepository elasticsearchRepository;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public SearchService(ElasticsearchRepository elasticsearchRepository) {
        this.elasticsearchRepository = elasticsearchRepository;
    }

    public List<ProductDto> searchProducts(String query, int size, Double lastScore, String lastAsin) throws IOException {
        SearchResponse<Product> searchResponse = elasticsearchRepository.searchProducts(query, size, lastScore, lastAsin);
        return convertToProductDTOs(searchResponse);
    }

    private List<ProductDto> convertToProductDTOs(SearchResponse<Product> searchResponse) {
        List<ProductDto> productDtos = new ArrayList<>();

        for (Hit<Product> hit : searchResponse.hits().hits()) {
            Product source = hit.source();
            ProductDto productDto = new ProductDto();
            productDto.setAsin(source.getAsin());
            productDto.setTitle(source.getTitle());
            productDto.setProduct_url(source.getProduct_url());
            productDto.setImg_url(source.getImg_url());
            productDto.setReviews(source.getReviews());
            productDto.setPrice(source.getPrice());
            productDto.setStars(source.getStars());
            productDto.setCategory_id(source.getCategory_id());
            productDto.setCreated_at(sdf.format(source.getCreated_at()));
            productDto.setUpdated_at(sdf.format(source.getUpdated_at()));

            Double score = hit.score();
            if (score != null) {
                productDto.setScore(score);
            }
            productDtos.add(productDto);
        }

        return productDtos;
    }
}