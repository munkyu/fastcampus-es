package com.fastcampus.indexer.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.indices.GetAliasRequest;
import co.elastic.clients.elasticsearch.indices.GetAliasResponse;
import com.fastcampus.indexer.entity.Product;
import com.fastcampus.indexer.helper.ESHelper;
import com.fastcampus.indexer.dto.ESProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DynamicIndexJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final ElasticsearchClient elasticsearchClient;

    private static final int CHUNK_SIZE = 1000;
    private static final int PAGE_SIZE = 1000;


    @Bean
    public Job dynamicIndexJob_build(Step dynamicIndexJob_step1) {
        return jobBuilderFactory.get("dynamicIndexJob")
                .start(dynamicIndexJob_step1)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step dynamicIndexJob_step1() {
        log.info("dynamicIndexJob_step1 start");
        return stepBuilderFactory.get("dynamicIndexJob_step1")
                .<Product, Product>chunk(CHUNK_SIZE)
                .reader(incrementProductsReader())
                .writer(dynamicIndexJob_elasticBulkWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Product> incrementProductsReader() {
        log.info("incrementProductsReader start");
        return new JpaPagingItemReader<>() {{
            setEntityManagerFactory(entityManagerFactory);
            setQueryString("SELECT p FROM Product p WHERE p.updatedAt > :lastUpdatedAt");
            // 1 minute ago
            Date lastUpdatedAt = new Date(System.currentTimeMillis() - (60 * 1000));
            setParameterValues(Collections.singletonMap("lastUpdatedAt", lastUpdatedAt));
            setPageSize(PAGE_SIZE);
        }};
    }


    @Bean
    @StepScope
    public ItemWriter<Product> dynamicIndexJob_elasticBulkWriter() {
        return products -> {
            GetAliasRequest request = GetAliasRequest.of(b -> b.name("products"));

            // Alias 존재 여부 확인 요청 실행
            GetAliasResponse response = elasticsearchClient.indices().getAlias(request);

            String currentIndex = response.result().keySet().iterator().next();

            log.info("Writing products: {}", products);
            BulkRequest.Builder br = new BulkRequest.Builder();

            for (Product product : products) {
                ESProductDto esProductDto = new ESProductDto(product.getId(), product.getAsin(), product.getTitle(), product.getImgUrl(), product.getProductUrl(), product.getStars(), product.getReviews(), product.getPrice(), product.getListPrice(), product.getCategoryId(), product.isBestSeller(), product.getBoughtInLastMonth(), product.isRecommendSeller(), product.getCreatedAt(), product.getUpdatedAt());
                br.operations(op -> op
                        .index(idx -> idx
                                .index(currentIndex)
                                .id("product_" + product.getId())
                                .document(esProductDto)
                        )
                );
            }

            ESHelper.DoBulk(elasticsearchClient, br);
        };
    }

}

