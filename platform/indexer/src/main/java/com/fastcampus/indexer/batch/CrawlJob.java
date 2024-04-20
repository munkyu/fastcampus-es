package com.fastcampus.indexer.batch;

import com.fastcampus.indexer.entity.Product;
import com.fastcampus.indexer.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class CrawlJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private static final  int CHUNK_SIZE = 1000;

    @Bean
    public Job crawlJob_build(Step crawlJob_step1) {
        return jobBuilderFactory.get("crawlJob")
                .start(crawlJob_step1)
                .build();
    }

    @Bean
    public Step crawlJob_step1() {
        return stepBuilderFactory.get("crawlJob_step1")
                .<ProductDto, Product>chunk(CHUNK_SIZE)
                .reader(crawlJob_csvItemReader())
                .processor(crawlJob_csvItemProcessor())
                .writer(crawlJob_itemWriter())
                .build();
    }

    private ItemProcessor<ProductDto, Product> crawlJob_csvItemProcessor() {
        return item -> {
            Product product = new Product();
            if (item.getStars() >= 4.5 &&
                    item.isBestSeller() && item.getBoughtInLastMonth() >= 200) {
                product.setRecommendSeller(true);
            }
            product.setId(null);
            product.setAsin(item.getAsin());
            product.setTitle(item.getTitle());
            product.setImgUrl(item.getImgUrl());
            product.setProductUrl(item.getProductUrl());
            product.setStars(item.getStars());
            product.setReviews(item.getReviews());
            product.setPrice(item.getPrice());
            product.setListPrice(item.getListPrice());
            product.setCategoryId(item.getCategoryId());
            product.setBestSeller(item.isBestSeller());
            product.setBoughtInLastMonth(item.getBoughtInLastMonth());
            return product;
        };
    }

    @Bean
    JpaItemWriter<Product> crawlJob_itemWriter() {
        JpaItemWriter<Product> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    @Bean
    public FlatFileItemReader<ProductDto> crawlJob_csvItemReader() {
        return new FlatFileItemReaderBuilder<ProductDto>()
                .name("csvItemReader")
                .resource(new ClassPathResource("product_sample.csv"))
                .linesToSkip(1)
                .delimited()
                .names("asin", "title", "img_url", "product_url", "stars", "reviews", "price", "list_price", "category_id", "is_best_seller", "bought_in_last_month")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(ProductDto.class);
                }})
                .build();
    }

}

