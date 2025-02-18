package com.example.springbatch_test1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringBatchTest1Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchTest1Application.class, args);
    }

}
