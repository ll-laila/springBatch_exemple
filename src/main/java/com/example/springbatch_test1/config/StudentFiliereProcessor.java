package com.example.springbatch_test1.config;

import com.example.springbatch_test1.entity.Student;
import com.example.springbatch_test1.entity.StudentFiliere;
import org.springframework.batch.item.ItemProcessor;



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


