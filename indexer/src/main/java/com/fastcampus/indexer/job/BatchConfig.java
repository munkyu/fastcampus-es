package com.fastcampus.indexer.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public BatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job exampleJob() {
        return jobBuilderFactory.get("exampleJob")
                .start(exampleStep())
                .build();
    }

    @Bean
    public Step exampleStep() {
        return stepBuilderFactory.get("exampleStep")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Example step executed");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}