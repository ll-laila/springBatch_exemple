package com.example.springbatch_test1.repo;

import com.example.springbatch_test1.entity.AcademicEmail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicEmailRepository extends JpaRepository<AcademicEmail, Integer> {
}
