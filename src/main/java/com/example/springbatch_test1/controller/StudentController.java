package com.example.springbatch_test1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor

public class StudentController {

    @Autowired
    private JobLauncher jobLauncher;


    @Autowired
    private Job job1;

    @Autowired
    private Job job2;

    @Autowired
    private Job job3;


    @PostMapping("/run-job1")
    public void runJob1() {
        launchJob(job1);
    }


    @PostMapping("/run-job2")
   // @Scheduled(fixedRate = 10000)  // Runs every 10 seconds
    public void runJob2() {
        launchJob(job2);
    }


    @PostMapping("/run-job3")
    public void runJob3() {
        launchJob(job3);   // using partitioning
    }



    private void launchJob(Job job) {
        JobParameters jobParameters = new JobParametersBuilder() //this is used to pass parameters to the batch job.
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(job, jobParameters);
        } catch (JobExecutionAlreadyRunningException
                 | JobRestartException
                 | JobInstanceAlreadyCompleteException
                 | JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }
}
