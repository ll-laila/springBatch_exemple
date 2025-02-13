package com.example.springbatch_test1.repo;

import com.example.springbatch_test1.entity.CourseStudent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseStudentRepository extends JpaRepository<CourseStudent, Integer> {

}
