package com.learningassistant.repositories;

import com.learningassistant.models.LearningMaterial;
import com.learningassistant.models.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, Long> {
    List<LearningMaterial> findByTopic(Topic topic);
    List<LearningMaterial> findByDifficultyLevelBetween(int minLevel, int maxLevel);

    // Method needed by QuizService.getRecommendedMaterialsForTopics
    List<LearningMaterial> findByTopicInOrderByDifficultyLevelAsc(List<Topic> topics);

    // Original method for finding by subject ID and difficulty
    @Query("SELECT m FROM LearningMaterial m WHERE m.topic.subject.id = :subjectId AND m.difficultyLevel = :level")
    List<LearningMaterial> findBySubjectIdAndDifficultyLevel(Long subjectId, int level);

    // Updated method with the correct property path
    List<LearningMaterial> findByLearningStyleAndTopicSubjectIdInAndDifficultyLevelBetween(
            String learningStyle, Set<Long> subjectIds, int minLevel, int maxLevel);

    List<LearningMaterial> findByLearningStyleAndDifficultyLevelBetween(
            String learningStyle, int minLevel, int maxLevel);

    // Updated to use topic.subject.name instead of subjectName
    @Query("SELECT m FROM LearningMaterial m WHERE m.topic.subject.name = :subjectName")
    List<LearningMaterial> findBySubjectName(String subjectName);

    // Updated to use topic.subject.name instead of subjectName
    @Query("SELECT m FROM LearningMaterial m WHERE m.topic.subject.name = :subjectName AND m.difficultyLevel BETWEEN :minLevel AND :maxLevel")
    List<LearningMaterial> findBySubjectNameAndDifficultyLevelBetween(String subjectName, int minLevel, int maxLevel);

    List<LearningMaterial> findByContentTypeContainingIgnoreCase(String contentType);

    // Updated to use topic.subject.name instead of subjectName
    @Query("SELECT m FROM LearningMaterial m WHERE m.topic.subject.name = :subjectName AND m.createdAt > :date")
    List<LearningMaterial> findBySubjectNameAndCreatedAtAfter(String subjectName, java.time.LocalDateTime date);
}