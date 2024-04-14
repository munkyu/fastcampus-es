package com.fastcampus.indexer.batch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.indices.GetAliasRequest;
import co.elastic.clients.elasticsearch.indices.GetAliasResponse;
import co.elastic.clients.elasticsearch.indices.UpdateAliasesRequest;
import co.elastic.clients.elasticsearch.indices.UpdateAliasesResponse;
import com.fastcampus.indexer.domain.Products;
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

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.sql.Date;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class FullJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;


    @Bean
    public Job fullJob_build(Step fullJob_step1, Step fullJob_step2, Step fullJob_step3) {
        return jobBuilderFactory.get("fullJob")
                .start(fullJob_step1)
                .next(fullJob_step2)
                .next(fullJob_step3)
                .build();
    }

    @Bean
    public Step fullJob_step1(ItemWriter<Products> fullJob_elasticBulkWriter) {
        return stepBuilderFactory.get("fullJob_step1")
                .<Products, Products>chunk(1000)
                .reader(fullItemReader())
                .writer(fullJob_elasticBulkWriter)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Products> fullItemReader() {
        return new JpaPagingItemReader<Products>() {{
            setEntityManagerFactory(entityManagerFactory);
            setQueryString("SELECT p FROM Products p");
            setPageSize(1000);
        }};
    }

    @Bean
    @JobScope
    public Step fullJob_step2(@Value("#{jobParameters[date]}") String date, ElasticsearchClient elasticsearchClient) {
        return stepBuilderFactory.get("fullJob_step2")
                .tasklet((contribution, chunkContext) -> {
                    changeElasticsearchAlias(date, elasticsearchClient);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step fullJob_step3(ItemWriter<Products> fullJob_elasticBulkWriter) {
        return stepBuilderFactory.get("fullJob_step3")
                .<Products, Products>chunk(1000)
                .reader(fullJob_incrementProductsReader())
                .writer(fullJob_elasticBulkWriter)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Products> fullJob_incrementProductsReader() {
        log.info("fullJob_incrementProductsReader start");
        return new JpaPagingItemReader<Products>() {{
            setEntityManagerFactory(entityManagerFactory);
            setQueryString("SELECT p FROM Products p WHERE p.updatedAt > :lastUpdatedAt");
            setParameterValues(Collections.singletonMap("lastUpdatedAt", new Date(System.currentTimeMillis() - 60 * 60 * 1000))); // 1시간 이내 변경된 데이터
            setPageSize(1000);
        }};
    }


    @Bean
    @StepScope
    @JobScope
    public ItemWriter<Products> fullJob_elasticBulkWriter(@Value("#{jobParameters[date]}") String date, ElasticsearchClient elasticsearchClient) {
        return products -> {
            log.info("Writing products: {}", products);
            BulkRequest.Builder br = new BulkRequest.Builder();

            for (Products product : products) {
                br.operations(op -> op
                        .index(idx -> idx
                                .index("products_" + date)
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

    private void changeElasticsearchAlias(String date, ElasticsearchClient elasticsearchClient) throws IOException {
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
        } else {
            log.error("Alias 교체 실패.");
        }
    }
}

