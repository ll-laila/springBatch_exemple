package com.example.springbatch.config.batch;

import com.example.springbatch.config.processor.CourseStudentProcessor;
import com.example.springbatch.config.processor.StudentFiliereProcessor;
import com.example.springbatch.config.reader.ReadersConfig;
import com.example.springbatch.config.writer.WritersConfig;
import com.example.springbatch.model.CourseStudent;
import com.example.springbatch.model.Student;
import com.example.springbatch.model.StudentFiliere;
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
public class ChunkJob2Config {

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


    private final CourseStudentProcessor courseStudentProcessor;

    private final StudentFiliereProcessor studentFiliereProcessor;

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;



    public ChunkJob2Config (JobRepository jobRepository, ReadersConfig readerConfig, WritersConfig writersConfig, CourseStudentProcessor courseStudentProcessor, PlatformTransactionManager platformTransactionManager, StudentFiliereProcessor studentFiliereProcessor){
        this.jobRepository = jobRepository;
        this.readerConfig = readerConfig;
        this.writersConfig = writersConfig;
        this.courseStudentProcessor = courseStudentProcessor;
        this.platformTransactionManager = platformTransactionManager;
        this.studentFiliereProcessor = studentFiliereProcessor;
    }



    @Bean(name = "job2")
    public Job job2() {
        return new JobBuilder("job2", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1())  //  db -> json  (students affectations)
                .next(step2()) // json -> txt   (students affectations)
                .next(step3())  // txt -> xml  (students affectations)
                .next(step4()) //  db -> pdf   (students courses)
                .next(step5()) // db -> html   (students courses)
                .build();
    }





    // Step pour effectuer le traitement d'affectation des étudiants aux filières
    public Step step1() {
        return new StepBuilder("Step1-dbToJson", jobRepository)
                .<Student, StudentFiliere>chunk(6, platformTransactionManager)
                .reader(readerConfig.studentDBReader())
                .processor(studentFiliereProcessor)
                .writer(writersConfig.studentFiliereDBWriter())
                .build();
    }



    public Step step2() {
        return new StepBuilder("Step2-jsonToTxt", jobRepository)
                .<StudentFiliere, StudentFiliere>chunk(40, platformTransactionManager)
                .reader(readerConfig.jsonFiliereReader())
                .writer(writersConfig.txtStudentFiliereWriter())
                .build();
    }


    public Step step3() {
        return new StepBuilder("Step3-txtToXml", jobRepository)
                .<StudentFiliere, StudentFiliere>chunk(40, platformTransactionManager)
                .reader(readerConfig.txtReader())
                .writer(writersConfig.xmlWriter1())
                .build();
    }


    public Step step4() {
        return new StepBuilder("Step4-dbToPdf", jobRepository)
                .<Student, CourseStudent>chunk(20, platformTransactionManager)
                .reader(readerConfig.studentDBReader())
                .processor(courseStudentProcessor)
                .writer(writersConfig.pdfCourseStudentWriter())
                .build();
    }


    public Step step5() {
        return new StepBuilder("Step5-dbToHtml", jobRepository)
                .<Student, CourseStudent>chunk(20, platformTransactionManager)
                .reader(readerConfig.studentDBReader())
                .processor(courseStudentProcessor)
                .writer(writersConfig.htmlCourseStudentWriter())
                .build();
    }





}
