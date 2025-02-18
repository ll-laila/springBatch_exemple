package com.example.springbatch_test1.repo;

import com.example.springbatch_test1.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {


    // Méthode pour récupérer l'ID minimum des étudiants
    @Query("SELECT MIN(s.id) FROM Student s")
    Integer findMinId();

    // Méthode pour récupérer l'ID maximum des étudiants
    @Query("SELECT MAX(s.id) FROM Student s")
    Integer findMaxId();

}
