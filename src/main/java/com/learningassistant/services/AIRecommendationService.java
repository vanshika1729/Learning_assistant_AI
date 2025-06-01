package com.learningassistant.services;

import com.learningassistant.models.LearningMaterial;
import com.learningassistant.models.StudentProfile;
import com.learningassistant.models.Subject;
import com.learningassistant.models.User;
import com.learningassistant.models.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that uses AI techniques to generate personalized learning material recommendations
 * based on student skills and learning preferences.
 */
@Service
public class AIRecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(AIRecommendationService.class);

    @Autowired
    private LearningMaterialService learningMaterialService;

    @Autowired
    private StudentProfileService profileService;

    /**
     * Generates personalized recommendations based on a specific skill the student wants to improve
     *
     * @param user The student user
     * @param skillName The specific skill to focus on
     * @return List of recommended learning materials
     */
    public List<LearningMaterial> generateSkillBasedRecommendations(User user, String skillName) {
        logger.info("Generating skill-based recommendations for user {} and skill {}", user.getUsername(), skillName);

        // Get user profile
        StudentProfile profile = profileService.getProfileByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user"));

        // Get the student's current skill level in the chosen subject
        Map<String, Integer> skills = profile.getSkills();
        Integer currentSkillLevel = skills.getOrDefault(skillName, 0);

        // Determine if the student is a beginner, intermediate, or advanced in this skill
        String skillLevel = categorizeSkillLevel(currentSkillLevel);

        // Get the student's learning style preference
        String learningStyle = profile.getPreferredLearningStyle();

        // Get interests that might be related to this skill
        Set<Subject> interests = profile.getInterests();

        // Get materials using AI-based filtering algorithm
        return applyAIRecommendationAlgorithm(skillName, skillLevel, learningStyle, interests);
    }

    /**
     * Categorizes the numerical skill level into beginner, intermediate, or advanced
     *
     * @param skillLevel Numerical skill level (1-10)
     * @return String representation of skill level category
     */
    private String categorizeSkillLevel(Integer skillLevel) {
        if (skillLevel == null || skillLevel < 3) {
            return "BEGINNER";
        } else if (skillLevel < 7) {
            return "INTERMEDIATE";
        } else {
            return "ADVANCED";
        }
    }

    /**
     * Applies an AI-based algorithm to recommend learning materials based on various factors
     *
     * @param skillName The skill name to focus on
     * @param skillLevel The categorized skill level (BEGINNER, INTERMEDIATE, ADVANCED)
     * @param learningStyle The student's preferred learning style
     * @param interests The student's interests that might relate to the skill
     * @return List of recommended learning materials
     */
    private List<LearningMaterial> applyAIRecommendationAlgorithm(
            String skillName,
            String skillLevel,
            String learningStyle,
            Set<Subject> interests) {

        logger.info("Applying AI recommendation algorithm for skill: {}, level: {}, style: {}",
                skillName, skillLevel, learningStyle);

        // Get base materials for the skill
        List<LearningMaterial> baseMaterials = learningMaterialService.findMaterialsBySubject(skillName);

        // Apply collaborative filtering based on skill level
        List<LearningMaterial> filteredByLevel = filterBySkillLevel(baseMaterials, skillLevel);

        // Apply content-based filtering based on learning style
        List<LearningMaterial> filteredByStyle = filterByLearningStyle(filteredByLevel, learningStyle);

        // Apply knowledge graph-based recommendations using related interests
        List<LearningMaterial> enhancedWithRelated = enhanceWithRelatedMaterials(filteredByStyle, interests);

        // Apply personalized scoring and sorting
        List<LearningMaterial> scoredMaterials = scoreAndSortMaterials(enhancedWithRelated, skillLevel, learningStyle);

        logger.info("Generated {} recommendations for skill: {}", scoredMaterials.size(), skillName);

        return scoredMaterials;
    }

    /**
     * Filters materials based on student's skill level
     */
    private List<LearningMaterial> filterBySkillLevel(List<LearningMaterial> materials, String skillLevel) {
        // Filter materials based on difficulty level matching the student's skill level
        return materials.stream()
                .filter(material -> isAppropriateForSkillLevel(material, skillLevel))
                .collect(Collectors.toList());
    }

    /**
     * Determines if a learning material is appropriate for the given skill level
     */
    private boolean isAppropriateForSkillLevel(LearningMaterial material, String skillLevel) {
        // Get the material difficulty and convert to String to ensure compatibility
        // This fixes the "incompatible types: java.lang.Integer cannot be converted to java.lang.String" error
        String materialDifficulty = String.valueOf(material.getDifficultyLevel());

        if (skillLevel.equals("BEGINNER")) {
            return materialDifficulty.equals("BEGINNER") || materialDifficulty.equals("INTRODUCTORY");
        } else if (skillLevel.equals("INTERMEDIATE")) {
            return materialDifficulty.equals("INTERMEDIATE") || materialDifficulty.equals("STANDARD");
        } else { // ADVANCED
            return materialDifficulty.equals("ADVANCED") || materialDifficulty.equals("EXPERT");
        }
    }

    /**
     * Filters materials based on student's preferred learning style
     */
    private List<LearningMaterial> filterByLearningStyle(List<LearningMaterial> materials, String learningStyle) {
        // First include perfect matches for learning style
        List<LearningMaterial> perfectMatches = materials.stream()
                .filter(material -> material.getContentType().toUpperCase().contains(learningStyle.toUpperCase()))
                .collect(Collectors.toList());

        // Then include other materials if we don't have enough perfect matches
        if (perfectMatches.size() < 5) {
            int neededAdditional = Math.min(5, materials.size()) - perfectMatches.size();

            List<LearningMaterial> additionalMaterials = materials.stream()
                    .filter(material -> !perfectMatches.contains(material))
                    .limit(neededAdditional)
                    .collect(Collectors.toList());

            perfectMatches.addAll(additionalMaterials);
        }

        return perfectMatches;
    }

    /**
     * Enhances recommendations with materials from related subjects based on student interests
     */
    private List<LearningMaterial> enhanceWithRelatedMaterials(
            List<LearningMaterial> materials,
            Set<Subject> interests) {

        // If we already have enough materials, return as is
        if (materials.size() >= 10) {
            return materials;
        }

        // Get related materials based on student interests
        List<LearningMaterial> relatedMaterials = new ArrayList<>();
        for (Subject interest : interests) {
            // Find materials related to this interest
            List<LearningMaterial> interestMaterials =
                    learningMaterialService.findMaterialsBySubject(interest.getName());

            // Add to related materials if not already in base materials
            for (LearningMaterial material : interestMaterials) {
                if (!materials.contains(material) && !relatedMaterials.contains(material)) {
                    relatedMaterials.add(material);

                    // Break if we have enough related materials
                    if (materials.size() + relatedMaterials.size() >= 10) {
                        break;
                    }
                }
            }

            // Break if we have enough total materials
            if (materials.size() + relatedMaterials.size() >= 10) {
                break;
            }
        }

        // Combine original and related materials
        List<LearningMaterial> enhancedMaterials = new ArrayList<>(materials);
        enhancedMaterials.addAll(relatedMaterials);

        return enhancedMaterials;
    }

    /**
     * Scores and sorts materials using personalized metrics
     */
    private List<LearningMaterial> scoreAndSortMaterials(
            List<LearningMaterial> materials,
            String skillLevel,
            String learningStyle) {

        // Score materials based on multiple factors
        Map<LearningMaterial, Double> materialScores = new HashMap<>();

        for (LearningMaterial material : materials) {
            double score = 0.0;

            // Score based on skill level match (0-5 points)
            if (isAppropriateForSkillLevel(material, skillLevel)) {
                score += 5.0;
            } else {
                // Give partial credit for near matches
                // Convert material difficulty to String to ensure compatibility
                String materialDifficultyStr = String.valueOf(material.getDifficultyLevel());

                if (skillLevel.equals("BEGINNER") && materialDifficultyStr.equals("INTERMEDIATE")) {
                    score += 2.0; // Slightly too advanced
                } else if (skillLevel.equals("ADVANCED") && materialDifficultyStr.equals("INTERMEDIATE")) {
                    score += 2.0; // Slightly too basic
                } else if (skillLevel.equals("INTERMEDIATE")) {
                    score += 1.0; // Any material has some value for intermediate learners
                }
            }

            // Score based on learning style match (0-5 points)
            if (material.getContentType().toUpperCase().contains(learningStyle.toUpperCase())) {
                score += 5.0;
            } else {
                // Give partial credit based on alternative learning styles
                if (learningStyle.equals("VISUAL") && material.getContentType().contains("VIDEO")) {
                    score += 3.0;
                } else if (learningStyle.equals("AUDITORY") && material.getContentType().contains("AUDIO")) {
                    score += 3.0;
                } else if (learningStyle.equals("READING") &&
                        (material.getContentType().contains("TEXT") || material.getContentType().contains("ARTICLE"))) {
                    score += 3.0;
                } else if (learningStyle.equals("KINESTHETIC") && material.getContentType().contains("INTERACTIVE")) {
                    score += 3.0;
                }
            }

            // Score based on popularity/ratings (0-3 points)
            score += Math.min(3.0, material.getAverageRating());

            // Score based on recency (0-2 points)
            // Assumes newer content is more relevant
            if (material.getCreatedAt() != null) {
                // Simplified - in reality would compare actual dates
                if (material.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusMonths(6))) {
                    score += 2.0;
                } else if (material.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusYears(1))) {
                    score += 1.0;
                }
            }

            materialScores.put(material, score);
        }

        // Sort materials by score (descending)
        return materials.stream()
                .sorted((m1, m2) -> Double.compare(materialScores.getOrDefault(m2, 0.0),
                        materialScores.getOrDefault(m1, 0.0)))
                .collect(Collectors.toList());
    }

    /**
     * Tracks student's learning progress and updates profile accordingly
     *
     * @param user The student user
     * @param materialId ID of the completed learning material
     * @param skillName The skill associated with the material
     * @param completionScore Score/rating given by the student (1-10)
     */
    @Transactional
    public void trackLearningProgress(User user, Long materialId, String skillName, int completionScore) {
        logger.info("Tracking learning progress for user {}, material {}", user.getUsername(), materialId);

        // Get user profile
        StudentProfile profile = profileService.getProfileByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user"));

        // Get current skill level
        Map<String, Integer> skills = profile.getSkills();
        int currentLevel = skills.getOrDefault(skillName, 1);

        // Calculate new skill level based on completion score and material difficulty
        LearningMaterial material = learningMaterialService.getMaterialById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Learning material not found"));

        // Convert difficulty level to String to ensure compatibility
        String difficultyLevel = String.valueOf(material.getDifficultyLevel());
        int difficultyFactor = getDifficultyFactor(difficultyLevel);
        int improvementPoints = calculateImprovementPoints(completionScore, difficultyFactor);

        // Update skill level but cap it at 10
        int newLevel = Math.min(10, currentLevel + improvementPoints);

        // Update the profile with new skill level
        profileService.updateSkill(user, skillName, newLevel);

        logger.info("Updated skill level for {} from {} to {}", skillName, currentLevel, newLevel);
    }

    /**
     * Calculates difficulty factor based on material level
     */
    private int getDifficultyFactor(String difficultyLevel) {
        switch (difficultyLevel.toUpperCase()) {
            case "BEGINNER":
            case "INTRODUCTORY":
                return 1;
            case "INTERMEDIATE":
            case "STANDARD":
                return 2;
            case "ADVANCED":
            case "EXPERT":
                return 3;
            default:
                return 1;
        }
    }

    /**
     * Calculates improvement points based on completion score and difficulty
     */
    private int calculateImprovementPoints(int completionScore, int difficultyFactor) {
        // Basic formula: higher scores on more difficult material = more improvement
        if (completionScore >= 8) {
            return difficultyFactor;
        } else if (completionScore >= 5) {
            return difficultyFactor / 2;
        } else {
            return 0; // Little to no improvement for low scores
        }
    }

    /**
     * Generates a personalized learning path for a student to improve a specific skill
     *
     * @param user The student user
     * @param skillName The skill to improve
     * @param targetLevel The target skill level (1-10)
     * @return Ordered list of learning materials forming a learning path
     */
    public List<LearningMaterial> generateLearningPath(User user, String skillName, int targetLevel) {
        logger.info("Generating learning path for user {} to reach level {} in {}",
                user.getUsername(), targetLevel, skillName);

        // Get user profile
        StudentProfile profile = profileService.getProfileByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user"));

        // Get current skill level
        Map<String, Integer> skills = profile.getSkills();
        int currentLevel = skills.getOrDefault(skillName, 0);

        // Check if target is achievable
        if (currentLevel >= targetLevel) {
            logger.info("User already at or above target level");
            return Collections.emptyList();
        }

        if (targetLevel > 10) {
            throw new IllegalArgumentException("Target level cannot exceed 10");
        }

        // Generate learning path with progressive difficulty
        List<LearningMaterial> learningPath = new ArrayList<>();

        // For each skill level between current and target, add appropriate materials
        for (int level = currentLevel + 1; level <= targetLevel; level++) {
            String requiredSkillLevel = categorizeSkillLevel(level);

            // Get materials appropriate for this level
            List<LearningMaterial> levelMaterials = learningMaterialService
                    .findMaterialsBySubjectAndDifficulty(skillName, requiredSkillLevel);

            // Apply learning style filter
            String learningStyle = profile.getPreferredLearningStyle();
            List<LearningMaterial> stylizedMaterials = filterByLearningStyle(levelMaterials, learningStyle);

            // Add top material for this level to the path
            if (!stylizedMaterials.isEmpty()) {
                learningPath.add(stylizedMaterials.get(0));
            }
        }

        return learningPath;
    }

    // Modified method to use topic name instead of subject name
    public List<LearningMaterial> findMaterialsBySubjectAndDifficulty(String subjectName, String difficultyLevel) {
        // This method would typically involve:
        // 1. Query for materials with the given subject name and difficulty level
        // 2. Sort them by relevance

        // Simplified implementation - would need to be expanded based on your app's logic
        return learningMaterialService.getAllMaterials().stream()
                .filter(material -> material.getTopic().getName().equals(subjectName) &&
                        String.valueOf(material.getDifficultyLevel()).equals(difficultyLevel))
                .collect(Collectors.toList());
    }
}