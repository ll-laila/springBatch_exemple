package com.example.springbatch_test1.config.processor;


import com.example.springbatch_test1.entity.Student;
import org.springframework.batch.item.ItemProcessor;


public class StudentProcessor implements ItemProcessor<Student,Student> {


    @Override
    public Student process(Student student) {
        Student student1 = new Student();
        student1.setId(student.getId());
        student1.setFirstName(student.getFirstName().toUpperCase());
        student1.setLastName(student.getLastName().toUpperCase());
        student1.setAge(student.getAge());
        student1.setEmail(student.getEmail());
        student1.setPhoneNumber(student.getPhoneNumber());
        student1.setNoteGenerale(student.getNoteGenerale());
        return student;
    }
}

