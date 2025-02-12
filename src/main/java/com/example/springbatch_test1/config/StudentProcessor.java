package com.example.springbatch_test1.config;


import com.example.springbatch_test1.student.Student;
import org.springframework.batch.item.ItemProcessor;


public class StudentProcessor implements ItemProcessor<Student,Student> {

    @Override
    public Student process(Student student) {
        return student;
    }
}

