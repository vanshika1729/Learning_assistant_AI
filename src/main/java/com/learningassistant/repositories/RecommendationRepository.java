package com.learningassistant.repositories;

import com.learningassistant.models.Recommendation;
import com.learningassistant.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    // Find all recommendations for a user, ordered by recommended date descending
    List<Recommendation> findByUserOrderByRecommendedAtDesc(User user);

    // Find unviewed recommendations for a user, ordered by recommended date descending
    List<Recommendation> findByUserAndIsViewedFalseOrderByRecommendedAtDesc(User user);
}