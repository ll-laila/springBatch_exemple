package com.example.springbatch.repository;

import com.example.springbatch.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByFiliere(String filiere);
}