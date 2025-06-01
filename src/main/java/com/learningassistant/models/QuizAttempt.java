package com.learningassistant.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(name = "start_time")
    private java.time.LocalDateTime startTime;

    @Column(name = "end_time")
    private java.time.LocalDateTime endTime;

    @Column(name = "score")
    private Double score;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL)
    private List<UserAnswer> userAnswers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        startTime = java.time.LocalDateTime.now();
    }

    /**
     * Sets the status of the quiz attempt
     * @param status The status to set (e.g., "IN_PROGRESS", "COMPLETED")
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the status of the quiz attempt
     * @return The current status
     */
    public String getStatus() {
        return this.status;
    }
}