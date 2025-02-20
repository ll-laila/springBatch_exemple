package com.example.springbatch.service.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;



@Service
public class JobScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("jobScheduledConfig")
    private final Job jobScheduled;

     public JobScheduler(JobLauncher jobLauncher, @Qualifier("jobScheduled") Job jobScheduled) {
        this.jobLauncher = jobLauncher;
        this.jobScheduled = jobScheduled;

     }

     //@Scheduled(cron = "0 0 * * * *" )
     public void secondJobStarter() {
        Map<String, JobParameter<?>> params = new HashMap<>();
        params.put("currentTime", new JobParameter<>(System.currentTimeMillis(), Long.class));

        JobParameters jobParameters = new JobParameters(params);

        try {
            JobExecution jobExecution = jobLauncher.run(jobScheduled, jobParameters);

            System.out.println("jobExecution = " + jobExecution);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Exception starting second job");
        }
    }





        //  Explication du cron "*/5 * * * * *"   ou bien fixedRate = 5000
        // */5 → Exécuter toutes les 5 secondes
        // * → Chaque minute
        // * → Chaque heure
        // * → Chaque jour
        // * → Chaque mois
        // * → Chaque jour de la semaine

}
