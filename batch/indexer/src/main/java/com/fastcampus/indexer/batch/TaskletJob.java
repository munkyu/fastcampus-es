package com.fastcampus.indexer.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TaskletJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job exampleJob() {
        return jobBuilderFactory.get("exampleJob")
                .start(exampleStep())
                .next(exampleStep2(null))
                .build();
    }

    @Bean
    public Step exampleStep() {
        return stepBuilderFactory.get("exampleStep")
                .tasklet((contribution, chunkContext) -> {
                    log.debug("Example step executed");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    @JobScope
    public Step exampleStep2(@Value("#{jobParameters[date]}") String date) {
        return stepBuilderFactory.get("exampleStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.debug("Example step2 executed: " + date);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
