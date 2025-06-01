// Topic.java
package com.learningassistant.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "topics")
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private Set<LearningMaterial> materials = new HashSet<>();

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private Set<Quiz> quizzes = new HashSet<>();
}
