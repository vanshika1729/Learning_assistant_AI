package com.learningassistant.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "learning_materials")
public class LearningMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;

    @Column(name = "material_type")
    @Enumerated(EnumType.STRING)
    private MaterialType type;

    @Column(name = "external_url")
    private String externalUrl;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel; // 1-10

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "learning_style")
    private String learningStyle;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "content_type")
    private String contentType;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public enum MaterialType {
        TEXT, VIDEO, PDF, EXERCISE, LINK
    }
}