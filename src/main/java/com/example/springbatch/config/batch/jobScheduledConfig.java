package com.example.springbatch.config.batch;

import com.example.springbatch.config.processor.StudentCountProcessor;
import com.example.springbatch.config.reader.ReadersConfig;
import com.example.springbatch.config.writer.WritersConfig;
import com.example.springbatch.model.Student;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
public class jobScheduledConfig {


    private final ReadersConfig readerConfig;

    private final WritersConfig writersConfig;

    private final StudentCountProcessor studentCountProcessor;

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;




    public jobScheduledConfig (JobRepository jobRepository, ReadersConfig readerConfig, WritersConfig writersConfig, PlatformTransactionManager platformTransactionManager, StudentCountProcessor studentCountProcessor ){
        this.jobRepository = jobRepository;
        this.readerConfig = readerConfig;
        this.writersConfig = writersConfig;
        this.studentCountProcessor = studentCountProcessor;
        this.platformTransactionManager = platformTransactionManager;
    }


    @Bean(name = "jobScheduled")
    public Job jobScheduled() {
        return new JobBuilder("jobScheduled", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step())
                .build();
    }


    public Step step() {
        return new StepBuilder("step-countStudents", jobRepository)
                .<Student, Integer>chunk(60, platformTransactionManager)
                .reader(readerConfig.studentDBReader())
                .processor(studentCountProcessor)
                .writer(writersConfig.studentCountWriter())
                .build();
    }




}
