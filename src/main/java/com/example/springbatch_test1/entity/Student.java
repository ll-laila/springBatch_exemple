package com.example.springbatch_test1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.*;

@Entity
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@XmlRootElement(name = "Student")
public class Student {

    @Id
    @EqualsAndHashCode.Include
    private Integer id;

    private String firstName;
    private String lastName;
    private String age;
    private String email;
    private String phoneNumber;
    private Double noteGenerale;




    @XmlElement(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }



    @XmlElement(name = "firstName")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }



    @XmlElement(name = "lastName")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }



    @XmlElement(name = "age")
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }


    @XmlElement(name = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    @XmlElement(name = "phoneNumber")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    @XmlElement(name = "noteGenerale")
    public double getNoteGenerale() {
        return noteGenerale;
    }

    public void setNoteGenerale(double noteGenerale) {
        this.noteGenerale = noteGenerale;
    }
}