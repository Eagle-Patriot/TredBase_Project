package com.example.Tredbase_payment_system.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long studentId;
    private String studentName;
    private Double balance;
    @ManyToMany
    @JoinTable(
            name = "parent_student",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_id")
    )
    private List<Parent> parents;
}
