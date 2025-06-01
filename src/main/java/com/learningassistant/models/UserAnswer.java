package com.learningassistant.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_answers")
public class UserAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private QuizAttempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne
    @JoinColumn(name = "selected_answer_id")
    private Answer selectedAnswer;

    @Lob
    @Column(name = "text_answer")
    private String textAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    /**
     * Sets the time spent on this question in seconds
     * @param timeSpent Time in seconds spent answering this question
     */
    public void setTimeSpent(Integer timeSpent) {
        this.timeSpentSeconds = timeSpent;
    }

    /**
     * Gets the time spent on this question in seconds
     * @return Time in seconds
     */
    public Integer getTimeSpent() {
        return this.timeSpentSeconds;
    }
}