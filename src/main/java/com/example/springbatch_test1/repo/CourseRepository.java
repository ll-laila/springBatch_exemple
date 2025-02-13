package com.example.springbatch_test1.repo;

import com.example.springbatch_test1.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByFiliere(String filiere);
}