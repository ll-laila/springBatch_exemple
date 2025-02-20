package com.example.springbatch.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class JobService {

    private final JobLauncher jobLauncher;

    private final Job job1;
    private final Job job2;
    private final Job job3;

    public JobService(JobLauncher jobLauncher,
                      Job job1, Job job2, Job job3) {
        this.jobLauncher = jobLauncher;
        this.job1 = job1;
        this.job2 = job2;
        this.job3 = job3;
    }



    @Async
    public void startJob(String jobName) {
        Map<String, JobParameter<?>> params = new HashMap<>();
        params.put("currentTime", new JobParameter<>(System.currentTimeMillis(), Long.class));

        JobParameters jobParameters = new JobParameters(params);

        try {
            JobExecution jobExecution;

            switch (jobName) {
                case "job1"             -> jobExecution = jobLauncher.run(job1, jobParameters);
                case "job2"             -> jobExecution = jobLauncher.run(job2, jobParameters);
                case "job3"             -> jobExecution = jobLauncher.run(job3, jobParameters);
                default                     -> throw new IllegalArgumentException("Invalid job name");
            }

            System.out.println("jobExecution = " + jobExecution.getId());
        } catch (Exception ex) {
            throw new IllegalArgumentException(String.format("Exception starting job %s", jobName));
        }
    }


}
