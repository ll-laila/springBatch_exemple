package com.example.springbatch_test1.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.EqualsAndHashCode;

@Entity
@XmlRootElement(name = "course")
public class Course {

    @Id
    @EqualsAndHashCode.Include
    private int id;
    private String name;
    private String department;
    private String filiere;

    public Course() {
    }

    public Course(int id, String name, String department, String filiere) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.filiere = filiere;
    }





    @XmlElement(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "department")
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }


    @XmlElement(name = "filiere")
    public String getFiliere() {
        return filiere;
    }

    public void setFiliere(String filiere) {
        this.filiere = filiere;
    }

    @Override
    public String toString() {
        return "Course [id=" + id + ", name=" + name + ", department=" + department + ", filiere=" + filiere + "]";
    }


}