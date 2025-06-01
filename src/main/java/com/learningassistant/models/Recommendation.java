package com.learningassistant.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "recommendations")
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private LearningMaterial material;

    @Column(name = "recommendation_type")
    private String recommendationType;  // Add this field that's used in the service

    @Column(name = "recommendation_reason")
    private String reason;

    @Column(name = "recommended_at")
    private LocalDateTime recommendedAt;

    @Column(name = "is_viewed")
    private boolean isViewed;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    @Column(name = "user_rating")
    private Integer userRating; // 1-5

    @Column(name = "rated_at")
    private LocalDateTime ratedAt;  // Add this field that's used in the service

    @PrePersist
    protected void onCreate() {
        recommendedAt = LocalDateTime.now();
        isViewed = false;
    }
}