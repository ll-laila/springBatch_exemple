package com.example.springbatch_test1.config.processor;

import com.example.springbatch_test1.entity.AcademicEmail;
import com.example.springbatch_test1.entity.Student;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class StudentToAcademicEmailProcessor implements ItemProcessor<Student, AcademicEmail> {

    @Override
    public AcademicEmail process(Student student) throws Exception {
        String email = student.getFirstName().toLowerCase() + "." + student.getLastName().toLowerCase() + "@uca.ac.ma";
        AcademicEmail academicEmail = new AcademicEmail();
        academicEmail.setStudentId(student.getId());
        academicEmail.setAcademicEmail(email);
        return academicEmail;
    }
}
