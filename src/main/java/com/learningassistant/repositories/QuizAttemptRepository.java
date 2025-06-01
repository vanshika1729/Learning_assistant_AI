// QuizAttemptRepository.java
package com.learningassistant.repositories;

import com.learningassistant.models.Quiz;
import com.learningassistant.models.QuizAttempt;
import com.learningassistant.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUser(User user);
    List<QuizAttempt> findByUserOrderByStartTimeDesc(User user);
    List<QuizAttempt> findByQuiz(Quiz quiz);
    List<QuizAttempt> findByUserAndQuiz(User user, Quiz quiz);
}
