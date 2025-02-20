package com.example.springbatch.config.batch;

import com.example.springbatch.config.partitioner.StudentCsvPartitioner;
import com.example.springbatch.config.partitioner.StudentPartitioner;
import com.example.springbatch.config.processor.StudentProcessor;
import com.example.springbatch.config.processor.StudentToAcademicEmailProcessor;
import com.example.springbatch.config.reader.ReadersConfig;
import com.example.springbatch.config.writer.WritersConfig;
import com.example.springbatch.model.AcademicEmail;
import com.example.springbatch.model.Student;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
public class ChunkJob3Config {

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


    private final JobRepository jobRepository;

    private final StudentProcessor studentProcessor;

    private final StudentToAcademicEmailProcessor studentToAcademicEmailProcessor;

    private final PlatformTransactionManager platformTransactionManager;



    public ChunkJob3Config (JobRepository jobRepository, ReadersConfig readerConfig, StudentProcessor studentProcessor, WritersConfig writersConfig, PlatformTransactionManager platformTransactionManager, StudentToAcademicEmailProcessor studentToAcademicEmailProcessor){
        this.jobRepository = jobRepository;
        this.readerConfig = readerConfig;
        this.studentProcessor = studentProcessor;
        this.studentToAcademicEmailProcessor = studentToAcademicEmailProcessor;
        this.writersConfig = writersConfig;
        this.platformTransactionManager = platformTransactionManager;
    }



    @Bean(name = "job3")
    public Job job3() {
        return new JobBuilder("job3", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(masterStep1()) //  csv -> db   (infos students)
                .next(masterStep2())  // db -> db   using partitioning
                .build();
    }






    /******************************* from csv to db using partitioner *************************************/


    @Bean
    public StudentCsvPartitioner studentCsvPartitioner(){
        return new StudentCsvPartitioner();
    }



    @Bean
    public Step workStep1() {
        return new StepBuilder("WorkStep1", jobRepository)
                .<Student, Student>chunk(1000, platformTransactionManager)
                .reader(readerConfig.partitionedCsvItemReader(null, null,null))
                .processor(studentProcessor)
                .writer(writersConfig.writer2())
                .build();
    }




    @Bean
    public Step masterStep1() {
        return new StepBuilder("MasterStep1", jobRepository)
                .partitioner(workStep1().getName(), studentCsvPartitioner())
                .step(workStep1())
                .gridSize(4)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }






    /******************************* from db to db using partitioner *************************************/
    /**
     * Cette étape automatise l'importation des données des étudiants à partir de la BD :
     * 1. Récuperation à partir de la table `student` using partitioning.
     * 2. Traitement et transformation des données (génération des email académique à partir de leurs noms).
     * 3. Stockage des données dans la table `academic-email` en BD.
     */


    @Bean
    public StudentPartitioner partitioner() {
        return new StudentPartitioner();
    }



    @Bean
    public Step workStep2() {
        return new StepBuilder("WorkStep2", jobRepository)
                .<Student, AcademicEmail>chunk(1000, platformTransactionManager)
                .reader(readerConfig.pagingItemReader(null, null,null))
                .processor(studentToAcademicEmailProcessor)
                .writer(writersConfig.academicEmaiDBlWriter())
                .build();
    }


    @Bean
    public Step masterStep2() {
        return new StepBuilder("MasterStep2", jobRepository)
                .partitioner(workStep2().getName(), partitioner())
                .step(workStep2())
                .gridSize(4)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }



}
