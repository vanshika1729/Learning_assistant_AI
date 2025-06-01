// QuizRepository.java
package com.learningassistant.repositories;

import com.learningassistant.models.Quiz;
import com.learningassistant.models.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByTopic(Topic topic);
    List<Quiz> findByDifficultyLevelBetween(int minLevel, int maxLevel);
    // Fixed method - Error in Image 4
    List<Quiz> findByTopicIn(List<Topic> weakTopics);
}