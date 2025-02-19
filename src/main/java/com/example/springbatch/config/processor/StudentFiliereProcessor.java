package com.example.springbatch.config.processor;

import com.example.springbatch.model.Student;
import com.example.springbatch.model.StudentFiliere;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;


@Component
public class StudentFiliereProcessor implements ItemProcessor<Student, StudentFiliere> {

    private static Integer id = 1;

    @Override
    public StudentFiliere process(Student student) throws Exception {

        StudentFiliere studentFiliere = new StudentFiliere();
        studentFiliere.setId(Long.valueOf(id++));
        studentFiliere.setFirstName(student.getFirstName());
        studentFiliere.setLastName(student.getLastName());
        studentFiliere.setEmail(student.getEmail());


        if(student.getNoteGenerale() >=15.0){
            studentFiliere.setFiliere("computer science");
        }

        if(student.getNoteGenerale() >=14.0 && student.getNoteGenerale() <=15.0){
            studentFiliere.setFiliere("electrical engineering");
        }

        if(student.getNoteGenerale() <=14.0){
            studentFiliere.setFiliere("industrial engineering");
        }

        return studentFiliere;
    }
}


