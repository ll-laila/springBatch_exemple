package com.example.springbatch.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
public class CourseStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String studentName;
    private String filiere;

    @ManyToMany
    @JoinTable(
            name = "student_courses",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses = new ArrayList<>();







    // Getters / Setters


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }


    public void setFiliere(String filiere) {
        this.filiere = filiere;
    }

    public String getFiliere() {
        return filiere;
    }


    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}
