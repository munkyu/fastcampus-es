package com.fastcampus.indexer.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.indices.GetAliasRequest;
import co.elastic.clients.elasticsearch.indices.GetAliasResponse;
import com.fastcampus.indexer.domain.Products;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.sql.Date;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class IncrementJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;


    @Bean
    public Job incrementJob_build(Step incrementJob_step1) {
        return jobBuilderFactory.get("incrementJob")
                .start(incrementJob_step1)
                .build();
    }

    @Bean
    public Step incrementJob_step1(ItemWriter<Products> incrementJob_elasticBulkWriter) {
        log.info("incrementJob_step1 start");
        return stepBuilderFactory.get("incrementJob_step1")
                .<Products, Products>chunk(1000)
                .reader(incrementProductsReader())
                .writer(incrementJob_elasticBulkWriter)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Products> incrementProductsReader() {
        log.info("incrementProductsReader start");
        return new JpaPagingItemReader<>() {{
            setEntityManagerFactory(entityManagerFactory);
            setQueryString("SELECT p FROM Products p WHERE p.updatedAt > :lastUpdatedAt");
            setParameterValues(Collections.singletonMap("lastUpdatedAt", new Date(System.currentTimeMillis() - 5 * 60 * 1000))); // 5분 전
            setPageSize(1000);
        }};
    }


    @Bean
    @StepScope
    public ItemWriter<Products> incrementJob_elasticBulkWriter(ElasticsearchClient elasticsearchClient) {
        return products -> {
            GetAliasRequest request = GetAliasRequest.of(b -> b.name("products"));

            // Alias 존재 여부 확인 요청 실행
            GetAliasResponse response = elasticsearchClient.indices().getAlias(request);

            String currentIndex = response.result().keySet().iterator().next();

            log.info("Writing products: {}", products);
            BulkRequest.Builder br = new BulkRequest.Builder();

            for (Products product : products) {
                br.operations(op -> op
                        .index(idx -> idx
                                .index(currentIndex)
                                .id("product_" + product.getId())
                                .document(product)
                        )
                );
            }

            BulkResponse result = elasticsearchClient.bulk(br.build());
            if (result.errors()) {
                log.error("Bulk had errors");
                for (BulkResponseItem item: result.items()) {
                    if (item.error() != null) {
                        log.error(item.error().reason());
                    }
                }
            }
        };
    }
}

