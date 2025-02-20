package com.example.springbatch.config.batch;

import com.example.springbatch.config.processor.CourseProcessor;
import com.example.springbatch.config.processor.StudentProcessor;
import com.example.springbatch.config.reader.ReadersConfig;
import com.example.springbatch.config.writer.WritersConfig;
import com.example.springbatch.listener.JobListener;
import com.example.springbatch.listener.StepListener;
import com.example.springbatch.model.Course;
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
public class ChunkJob1Config {

    /**
     * This object contains all configured readers methods.<br>
     * For add more readers, must do it in `ReadersConfig` class.
     * */
    private final ReadersConfig readerConfig;


    /**
     * This object contains all configured writers methods.<br>
     * For add more readers, must do it in `WritersConfing` class.
     */
    private final WritersConfig writersConfig;

    private final StudentProcessor studentProcessor;
    private final CourseProcessor courseProcessor;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final JobListener jobListener;
    private final StepListener stepListener;


    public ChunkJob1Config (JobRepository jobRepository,ReadersConfig readerConfig,WritersConfig writersConfig,StudentProcessor studentProcessor,CourseProcessor courseProcessor,PlatformTransactionManager platformTransactionManager,  JobListener jobListener, StepListener stepListener){
        this.jobRepository = jobRepository;
        this.readerConfig = readerConfig;
        this.writersConfig = writersConfig;
        this.studentProcessor = studentProcessor;
        this.courseProcessor = courseProcessor;
        this.platformTransactionManager = platformTransactionManager;
        this.jobListener = jobListener;
        this.stepListener = stepListener;
    }



    @Bean(name = "job1")
    public Job job1() {
        return new JobBuilder("job1", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1()) //  csv -> db   (infos students)
                .next(step2()) //   json -> db  (infos students)
                .next(step3()) //  excel to db  (infos students)
                .next(step4()) // xml -> db     (infos students)
                .next(step5()) // csv to xml    (infos students)
                .next(step6()) // db to xml     (infos courses)*/
               // .listener(firstJobListener)
                .build();
    }




    public Step step1() {
        return new StepBuilder("step1-CsvToDb", jobRepository)
                .<Student, Student>chunk(20, platformTransactionManager)
                .reader(readerConfig.csvReader())
                .processor(studentProcessor)
                .writer(writersConfig.writer1())
                //.listener(firstStepListener)
                .build();
    }




    public Step step2() {
        return new StepBuilder("Step2-JsonToDb", jobRepository)
                .<Student, Student>chunk(20, platformTransactionManager)
                .reader(readerConfig.jsonReader())
                .processor(studentProcessor)
                .writer(writersConfig.writer1())
                .build();
    }




    public Step step3() {
        return new StepBuilder("Step3-ExcelToDb", jobRepository)
                .<Student, Student>chunk(10, platformTransactionManager)
                .reader(readerConfig.excelReader())
                .processor(studentProcessor)
                .writer(writersConfig.excelWriter())
                .build();
    }




    public Step step4() {
        return new StepBuilder("Step4-DbToXml", jobRepository)
                .<Student, Student>chunk(60, platformTransactionManager)
                .reader(readerConfig.studentDBReader())
                .processor(studentProcessor)
                .writer(writersConfig.xmlWriter3())
                .build();
    }



    public Step step5() {
        return new StepBuilder("Step5-CsvToXml", jobRepository)
                .<Student, Student>chunk(20, platformTransactionManager)
                .reader(readerConfig.csvReader())
                .processor(studentProcessor)
                .writer(writersConfig.xmlWriter2())
                .build();
    }


    public Step step6() {
        return new StepBuilder("Step6-xmlToDB", jobRepository)
                .<Course, Course>chunk(3, platformTransactionManager)
                .reader(readerConfig.xmlReader())
                .processor(courseProcessor)
                .writer(writersConfig.dbWriter())
                .build();
    }





}
