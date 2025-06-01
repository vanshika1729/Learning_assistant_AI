package com.learningassistant.repositories;

import com.learningassistant.models.Answer;
import com.learningassistant.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    // Remove the problematic method that's causing the error
    // Optional<Answer> findByQuestionAndCorrect(Question question, boolean correct);

    // Use the correct property name that matches the entity
    Optional<Answer> findByQuestionAndIsCorrect(Question question, boolean isCorrect);

    List<Answer> findByQuestion(Question question);
}