// QuestionRepository.java
package com.learningassistant.repositories;

import com.learningassistant.models.Question;
import com.learningassistant.models.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuiz(Quiz quiz);
    List<Question> findByDifficultyLevelBetween(int minLevel, int maxLevel);
}
