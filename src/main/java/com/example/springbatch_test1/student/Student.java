package com.example.springbatch_test1.student;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class Student {

    @Id
    @EqualsAndHashCode.Include
    private Integer id;

    private String firstName;
    private String lastName;
    private String age;

}