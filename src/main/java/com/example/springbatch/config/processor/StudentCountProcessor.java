package com.example.springbatch.config.processor;

import com.example.springbatch.model.Student;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;



@Component
public class StudentCountProcessor implements ItemProcessor<Student, Integer> {
    @Override
    public Integer process(Student student) {
        return 1; // Chaque étudiant compte pour 1
    }
}

