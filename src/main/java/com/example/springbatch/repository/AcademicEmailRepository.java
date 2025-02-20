package com.example.springbatch.repository;

import com.example.springbatch.model.AcademicEmail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicEmailRepository extends JpaRepository<AcademicEmail, Integer> {
}
