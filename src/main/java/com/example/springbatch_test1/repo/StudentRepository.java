package com.example.springbatch_test1.repo;

import com.example.springbatch_test1.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
}
