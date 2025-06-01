// Subject.java
package com.learningassistant.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "subjects")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private Set<Topic> topics = new HashSet<>();

    @ManyToMany(mappedBy = "interests")
    private Set<StudentProfile> interestedStudents = new HashSet<>();
}

