package com.fastcampus.indexer.scheduler;

import com.fastcampus.indexer.batch.DynamicIndexJob;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DynamicJobScheduler {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private DynamicIndexJob job;

    @Scheduled(cron = "0/10 * * * * *")
    public void runJob() throws Exception {
        JobParameters parameters = new JobParametersBuilder()
                .addString("jobName", "dynamicIndexJob")
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(job.dynamicIndexJob_build(job.dynamicIndexJob_step1()), parameters);
    }
}
