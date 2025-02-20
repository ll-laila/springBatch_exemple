package com.example.springbatch.config.processor;

import com.example.springbatch.model.Course;
import com.example.springbatch.model.CourseStudent;
import com.example.springbatch.model.Student;
import com.example.springbatch.repository.CourseRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseStudentProcessor  implements ItemProcessor<Student, CourseStudent> {


    @Autowired
    private CourseRepository courseRepository;


    @Override
    public CourseStudent process(Student student) throws Exception {

        CourseStudent courseStudent = new CourseStudent();

        courseStudent.setStudentName(student.getFirstName() + " " + student.getLastName());

        String filiere;
        if (student.getNoteGenerale() >= 15.0) {
            filiere = "computer science";
        } else if (student.getNoteGenerale() >= 14.0) {
            filiere = "electrical engineering";
        } else {
            filiere = "industrial engineering";
        }
        courseStudent.setFiliere(filiere);

        // Récupérer uniquement les cours de la même filière
        List<Course> courses = courseRepository.findByFiliere(filiere);
        courseStudent.setCourses(courses);

        return courseStudent;
    }

}
