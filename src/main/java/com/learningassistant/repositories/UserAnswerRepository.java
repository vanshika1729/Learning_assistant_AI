// UserAnswerRepository.java
package com.learningassistant.repositories;

import com.learningassistant.models.Question;
import com.learningassistant.models.QuizAttempt;
import com.learningassistant.models.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    List<UserAnswer> findByAttempt(QuizAttempt attempt);
    Optional<UserAnswer> findByAttemptAndQuestion(QuizAttempt attempt, Question question);
}
