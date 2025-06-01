package com.learningassistant.services;

import com.learningassistant.models.LearningMaterial;
import com.learningassistant.models.Recommendation;
import com.learningassistant.models.StudentProfile;
import com.learningassistant.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.learningassistant.repositories.RecommendationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RecommendationService {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private LearningMaterialService learningMaterialService;

    /**
     * Retrieves all recommendations for a specific user
     *
     * @param user The user to get recommendations for
     * @return List of recommendations for the user
     */
    public List<Recommendation> getUserRecommendations(User user) {
        return recommendationRepository.findByUserOrderByRecommendedAtDesc(user);
    }

    /**
     * Retrieves new (unviewed) recommendations for a specific user
     *
     * @param user The user to get new recommendations for
     * @return List of unviewed recommendations for the user
     */
    public List<Recommendation> getNewRecommendations(User user) {
        return recommendationRepository.findByUserAndIsViewedFalseOrderByRecommendedAtDesc(user);
    }

    /**
     * Retrieves a recommendation by its ID
     *
     * @param id The ID of the recommendation to retrieve
     * @return An Optional containing the Recommendation if found, or empty if not found
     */
    public Optional<Recommendation> getRecommendationById(Long id) {
        return recommendationRepository.findById(id);
    }

    /**
     * Marks a recommendation as viewed
     *
     * @param id The ID of the recommendation to mark as viewed
     * @return The updated recommendation
     */
    public Recommendation markRecommendationAsViewed(Long id) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid recommendation ID: " + id));

        recommendation.setViewed(true);
        recommendation.setViewedAt(LocalDateTime.now());

        return recommendationRepository.save(recommendation);
    }

    /**
     * Rates a recommendation with a value from 1-5
     *
     * @param id The ID of the recommendation to rate
     * @param rating The rating value (1-5)
     * @return The updated recommendation
     */
    public Recommendation rateRecommendation(Long id, Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid recommendation ID: " + id));

        recommendation.setUserRating(rating);
        recommendation.setRatedAt(LocalDateTime.now());

        return recommendationRepository.save(recommendation);
    }

    /**
     * Generates recommendations based on a user's profile
     *
     * @param user The user to generate recommendations for
     * @param profile The user's profile containing learning preferences
     */
    public void generateRecommendationsBasedOnProfile(User user, StudentProfile profile) {
        // Get learning materials based on user preferences in profile
        // Adapt to use the actual fields and methods in StudentProfile
        List<LearningMaterial> materials = learningMaterialService.findRelevantMaterials(
                profile.getPreferredLearningStyle(),
                profile.getInterests(),
                // Since there's no difficulty level field, passing null or using average skill level
                calculateAverageSkillLevel(profile.getSkills())
        );

        // Create new recommendations
        for (LearningMaterial material : materials) {
            Recommendation recommendation = new Recommendation();
            recommendation.setUser(user);
            recommendation.setMaterial(material);
            recommendation.setRecommendationType("PROFILE_BASED");
            recommendation.setReason("Based on your learning profile preferences");
            // No need to set recommendedAt as it's handled by @PrePersist

            recommendationRepository.save(recommendation);
        }
    }

    /**
     * Helper method to calculate average skill level from skills map
     */
    private int calculateAverageSkillLevel(java.util.Map<String, Integer> skills) {
        if (skills == null || skills.isEmpty()) {
            return 1; // Default to beginner level if no skills are specified
        }

        int sum = skills.values().stream().mapToInt(Integer::intValue).sum();
        return Math.max(1, Math.min(5, sum / skills.size())); // Ensure it's between 1-5
    }

    /**
     * Generates recommendations based on quiz results
     *
     * @param user The user to generate recommendations for
     * @param attemptId The ID of the quiz attempt to analyze
     */
    public void generateRecommendationsBasedOnQuiz(User user, Long attemptId) {
        // Implement logic to analyze quiz results and recommend materials
        // This would typically involve:
        // 1. Get the quiz attempt and its answers
        // 2. Identify weak areas or topics that need improvement
        // 3. Find relevant learning materials for those areas
        // 4. Create recommendations

        // For example (simplified):
        List<LearningMaterial> materials = learningMaterialService.findMaterialsForQuizImprovement(attemptId);

        for (LearningMaterial material : materials) {
            Recommendation recommendation = new Recommendation();
            recommendation.setUser(user);
            recommendation.setMaterial(material);
            recommendation.setRecommendationType("QUIZ_BASED");
            recommendation.setReason("Based on your recent quiz performance");
            // No need to set recommendedAt as it's handled by @PrePersist

            recommendationRepository.save(recommendation);
        }
    }
}