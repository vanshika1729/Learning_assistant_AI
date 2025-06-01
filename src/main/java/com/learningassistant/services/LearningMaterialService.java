package com.learningassistant.services;

import com.learningassistant.models.LearningMaterial;
import com.learningassistant.models.Topic;
import com.learningassistant.models.Subject;
import com.learningassistant.repositories.LearningMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LearningMaterialService {
    private final LearningMaterialRepository materialRepository;

    @Autowired
    public LearningMaterialService(LearningMaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public List<LearningMaterial> getAllMaterials() {
        return materialRepository.findAll();
    }

    public List<LearningMaterial> getMaterialsByTopic(Topic topic) {
        return materialRepository.findByTopic(topic);
    }

    public List<LearningMaterial> getMaterialsByDifficultyRange(int minLevel, int maxLevel) {
        return materialRepository.findByDifficultyLevelBetween(minLevel, maxLevel);
    }

    public List<LearningMaterial> getMaterialsBySubjectAndDifficulty(Long subjectId, int level) {
        return materialRepository.findBySubjectIdAndDifficultyLevel(subjectId, level);
    }

    /**
     * Find materials by subject name and difficulty level string (BEGINNER, INTERMEDIATE, etc.)
     * Added to support AIRecommendationService.generateLearningPath
     */
    public List<LearningMaterial> findMaterialsBySubjectAndDifficulty(String subjectName, String difficultyLevel) {
        // Convert string difficulty level to appropriate numerical range
        int minLevel = 1;
        int maxLevel = 2;

        if (difficultyLevel.equals("INTERMEDIATE")) {
            minLevel = 3;
            maxLevel = 6;
        } else if (difficultyLevel.equals("ADVANCED")) {
            minLevel = 7;
            maxLevel = 10;
        }

        return materialRepository.findBySubjectNameAndDifficultyLevelBetween(subjectName, minLevel, maxLevel);
    }

    /**
     * Find materials by subject name
     * Added to support AIRecommendationService.applyAIRecommendationAlgorithm
     */
    public List<LearningMaterial> findMaterialsBySubject(String subjectName) {
        return materialRepository.findBySubjectName(subjectName);
    }

    public Optional<LearningMaterial> getMaterialById(Long id) {
        return materialRepository.findById(id);
    }

    @Transactional
    public LearningMaterial createMaterial(LearningMaterial material) {
        return materialRepository.save(material);
    }

    @Transactional
    public LearningMaterial updateMaterial(LearningMaterial material) {
        return materialRepository.save(material);
    }

    @Transactional
    public void deleteMaterial(Long id) {
        materialRepository.deleteById(id);
    }

    /**
     * Rate a learning material and update its average rating
     * Added to support learning progress tracking
     */
    @Transactional
    public void rateMaterial(Long materialId, int rating) {
        if (rating < 1 || rating > 10) {
            throw new IllegalArgumentException("Rating must be between 1 and 10");
        }

        LearningMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found"));

        // Update the material's rating data
        int currentRatingCount = material.getRatingCount();
        double currentAverage = material.getAverageRating();

        // Calculate new average
        double newAverage = ((currentAverage * currentRatingCount) + rating) / (currentRatingCount + 1);

        // Update material
        material.setAverageRating(newAverage);
        material.setRatingCount(currentRatingCount + 1);

        materialRepository.save(material);
    }

    /**
     * Tracks user completion of materials
     * Added to support learning path progress tracking
     */
    @Transactional
    public void trackMaterialCompletion(Long userId, Long materialId) {
        // In a real implementation, this would record the completion in a join table
        // For now, we'll just add a placeholder
        // completionRepository.save(new MaterialCompletion(userId, materialId, LocalDateTime.now()));
    }

    /**
     * Finds learning materials relevant to the user's preferences
     *
     * @param learningStyle The preferred learning style
     * @param interests The subjects the user is interested in
     * @param difficultyLevel The appropriate difficulty level (1-5)
     * @return List of relevant learning materials
     */
    public List<LearningMaterial> findRelevantMaterials(
            String learningStyle,
            Set<Subject> interests,
            int difficultyLevel) {

        // Adjust difficulty range to include slightly easier and harder materials
        int minLevel = Math.max(1, difficultyLevel - 1);
        int maxLevel = Math.min(5, difficultyLevel + 1);

        if (interests != null && !interests.isEmpty()) {
            // Extract subject IDs
            Set<Long> subjectIds = interests.stream()
                    .map(Subject::getId)
                    .collect(Collectors.toSet());

            // Use the custom query method with topic.subject.id
            return materialRepository.findByLearningStyleAndTopicSubjectIdInAndDifficultyLevelBetween(
                    learningStyle, subjectIds, minLevel, maxLevel);
        } else {
            // If no specific subjects are provided, return materials just based on
            // learning style and difficulty
            return materialRepository.findByLearningStyleAndDifficultyLevelBetween(
                    learningStyle, minLevel, maxLevel);
        }
    }

    /**
     * Finds materials that match a specific learning style
     * Added to support AIRecommendationService filtering
     */
    public List<LearningMaterial> findMaterialsByLearningStyle(String learningStyle) {
        return materialRepository.findByContentTypeContainingIgnoreCase(learningStyle);
    }

    /**
     * Find recently added materials for a specific subject
     * Added to support recency scoring in AI recommendations
     */
    public List<LearningMaterial> findRecentMaterialsBySubject(String subjectName, int monthsBack) {
        return materialRepository.findBySubjectNameAndCreatedAtAfter(
                subjectName,
                java.time.LocalDateTime.now().minusMonths(monthsBack));
    }

    /**
     * Finds materials that can help improve quiz performance
     *
     * @param attemptId ID of the quiz attempt to analyze
     * @return List of relevant learning materials
     */
    public List<LearningMaterial> findMaterialsForQuizImprovement(Long attemptId) {
        // This would typically involve:
        // 1. Get the quiz attempt and analyze where the student struggled
        // 2. Identify topics/concepts that need reinforcement
        // 3. Find materials that cover those topics

        // This is a simplified implementation - you'll need to expand based on your app's logic
        // For now, returning an empty list as a placeholder
        return List.of();
    }
}