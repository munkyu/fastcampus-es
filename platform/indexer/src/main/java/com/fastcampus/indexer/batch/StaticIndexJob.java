package com.fastcampus.indexer.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.indices.*;
import com.fastcampus.indexer.entity.Product;
import com.fastcampus.indexer.helper.ESHelper;
import com.fastcampus.indexer.dto.ESProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class StaticIndexJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final ElasticsearchClient elasticsearchClient;

    private static final int CHUNK_SIZE = 1000;
    private static final int PAGE_SIZE = 1000;

    private static final int POOL_SIZE = 5;

    private Date lastUpdatedAt;

    @Bean
    public Job staticIndexJob_build(Step staticIndexJob_step1, Step staticIndexJob_step2, Step staticIndexJob_step3) {
        return jobBuilderFactory.get("staticIndexJob")
                .start(staticIndexJob_step1)
                .next(staticIndexJob_step2)
                .next(staticIndexJob_step3)
                .build();
    }

    @Bean
    public Step staticIndexJob_step1(ItemWriter<Product> staticIndexJob_elasticBulkWriter) {
        log.debug("staticIndex_step1 start");
        lastUpdatedAt = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.debug("lastUpdatedAt: {}", formatter.format(lastUpdatedAt));
        return stepBuilderFactory.get("staticIndexJob_step1")
                .<Product, Product>chunk(CHUNK_SIZE)
                .reader(fullItemReader())
                .writer(staticIndexJob_elasticBulkWriter)
                .taskExecutor(executor())
                .throttleLimit(POOL_SIZE)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Product> fullItemReader() {
        return new JpaPagingItemReader<>() {{
            setEntityManagerFactory(entityManagerFactory);
            setQueryString("SELECT p FROM Product p");
            setPageSize(PAGE_SIZE);
        }};
    }

    @Bean
    public Step staticIndexJob_step2(ItemWriter<Product> staticIndexJob_elasticBulkWriter) {
        log.debug("staticIndexJob_step2 start");
        return stepBuilderFactory.get("staticIndexJob_step2")
                .<Product, Product>chunk(CHUNK_SIZE)
                .reader(staticIndexJob_incrementProductsReader())
                .writer(staticIndexJob_elasticBulkWriter)
                .taskExecutor(executor())
                .throttleLimit(POOL_SIZE)
                .build();
    }

    @Bean
    @JobScope
    public Step staticIndexJob_step3(@Value("#{jobParameters[date]}") String date) {
        log.debug("staticIndexJob_step3 start");
        return stepBuilderFactory.get("staticIndexJob_step3")
                .tasklet((contribution, chunkContext) -> {
                    changeElasticsearchAlias(date);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public JpaPagingItemReader<Product> staticIndexJob_incrementProductsReader() {
        log.info("staticIndexJob_incrementProductsReader start");
        return new JpaPagingItemReader<>() {{
            setEntityManagerFactory(entityManagerFactory);
            setQueryString("SELECT p FROM Product p WHERE p.updatedAt > :lastUpdatedAt");
            setParameterValues(Collections.singletonMap("lastUpdatedAt", lastUpdatedAt));
            setPageSize(CHUNK_SIZE);
        }};
    }


    @Bean
    @StepScope
    @JobScope
    public ItemWriter<Product> staticIndexJob_elasticBulkWriter(@Value("#{jobParameters[date]}") String date, ElasticsearchClient elasticsearchClient) {
        return products -> {
            log.info("Writing products: {}", products);
            BulkRequest.Builder br = new BulkRequest.Builder();


            for (Product product : products) {
                ESProductDto esProductDto = new ESProductDto(product.getId(), product.getAsin(), product.getTitle(), product.getImgUrl(), product.getProductUrl(), product.getStars(), product.getReviews(), product.getPrice(), product.getListPrice(), product.getCategoryId(), product.isBestSeller(), product.getBoughtInLastMonth(), product.isRecommendSeller(), product.getCreatedAt(), product.getUpdatedAt());
                br.operations(op -> op
                        .index(idx -> idx
                                .index("products_" + date)
                                .id("product_" + product.getId())
                                .document(esProductDto)
                        )
                );
            }

            ESHelper.DoBulk(elasticsearchClient, br);
        };
    }


    private void changeElasticsearchAlias(String date) throws IOException {
        // Alias 존재 여부 확인
        GetAliasRequest request = GetAliasRequest.of(b -> b.name("products"));

        // Alias 존재 여부 확인 요청 실행
        GetAliasResponse response = elasticsearchClient.indices().getAlias(request);

        // get first key
        String oldIndex = response.result().keySet().iterator().next();
        String newIndexName = "products_" + date;

        log.info("Alias '{}'가 '{}'로 교체됩니다.", oldIndex, newIndexName);

        UpdateAliasesRequest.Builder builder = new UpdateAliasesRequest.Builder();

        builder.actions(actions -> actions
                .remove(remove -> remove
                        .index(oldIndex)
                        .alias("products"))
        );

        builder.actions(actions -> actions
                .add(add -> add
                        .index(newIndexName)
                        .alias("products"))
        );

        UpdateAliasesResponse updateAliasesResponse = elasticsearchClient.indices().updateAliases(builder.build());

        if (updateAliasesResponse.acknowledged()) {
            log.info("'products' alias가 성공적으로 교체되었습니다.");
            removeOldIndex(oldIndex);

        } else {
            log.error("Alias 교체 실패.");
        }
    }

    // remove old index
    private void removeOldIndex(String oldIndex) throws IOException {
        DeleteIndexRequest request = DeleteIndexRequest.of(b -> b.index(oldIndex));
        DeleteIndexResponse response = elasticsearchClient.indices().delete(request);
        if (response.acknowledged()) {
            log.info("Old index '{}'가 성공적으로 삭제되었습니다.", oldIndex);
        } else {
            log.error("Old index '{}' 삭제 실패.", oldIndex);
        }
    }


    @Bean
    TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(POOL_SIZE);
        executor.setMaxPoolSize(POOL_SIZE);
        executor.setThreadNamePrefix("multi-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
    }
}


