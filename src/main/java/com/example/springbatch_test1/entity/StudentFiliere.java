package com.example.springbatch_test1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;


@Getter
@Setter
@Entity
@XmlRootElement(name = "StudentFiliere")
public class StudentFiliere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String filiere;

    public StudentFiliere() {}

    public StudentFiliere(String studentId, String studentName, String email, String filiere) {
        this.id = Long.parseLong(studentId);
        String[] nameParts = studentName.trim().split("\\s+", 2);
        this.firstName = nameParts[0]; // Premier mot = prénom
        this.lastName = nameParts.length > 1 ? nameParts[1] : ""; // Le reste = nom (si disponible)
        this.email = email;
        this.filiere = filiere;
    }



    @XmlElement(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @XmlElement(name = "firstName")
    public String getFirstName() {
        return firstName;
    }


    @XmlElement(name = "lastName")
    public String getLastName() {
        return lastName;
    }


    @XmlElement(name = "email")
    public String getEmail() {
        return email;
    }


    @XmlElement(name = "filiere")
    public String getFiliere() {
        return filiere;
    }


    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public void setFiliere(String filiere) {
        this.filiere = filiere;
    }
}