package com.example.springbatch_test1.config;

import com.example.springbatch_test1.entity.Course;
import com.example.springbatch_test1.entity.CourseStudent;
import com.example.springbatch_test1.entity.Student;
import com.example.springbatch_test1.repo.CourseRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

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
