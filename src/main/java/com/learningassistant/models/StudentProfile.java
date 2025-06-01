// StudentProfile.java
package com.learningassistant.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "student_profiles")
public class StudentProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "education_level")
    private String educationLevel;

    @Column(name = "preferred_learning_style")
    private String preferredLearningStyle;

    @ManyToMany
    @JoinTable(
            name = "student_interests",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private Set<Subject> interests = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "student_skills", joinColumns = @JoinColumn(name = "profile_id"))
    @MapKeyColumn(name = "subject_name")
    @Column(name = "proficiency_level")
    private java.util.Map<String, Integer> skills = new java.util.HashMap<>();
}