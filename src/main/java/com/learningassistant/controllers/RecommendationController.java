// RecommendationController.java
package com.learningassistant.controllers;

import com.learningassistant.models.Recommendation;
import com.learningassistant.models.StudentProfile;
import com.learningassistant.models.User;
import com.learningassistant.services.RecommendationService;
import com.learningassistant.services.StudentProfileService;
import com.learningassistant.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserService userService;

    @Autowired
    private StudentProfileService profileService;

    @GetMapping
    public String viewRecommendations(Model model) {
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Get all recommendations
        List<Recommendation> recommendations = recommendationService.getUserRecommendations(user);

        model.addAttribute("recommendations", recommendations);

        return "recommendations/list";
    }

    @GetMapping("/{id}")
    public String viewRecommendation(@PathVariable Long id, Model model) {
        // Get recommendation
        Recommendation recommendation = recommendationService.getRecommendationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid recommendation ID: " + id));

        // Mark as viewed
        if (!recommendation.isViewed()) {
            recommendationService.markRecommendationAsViewed(id);
        }

        model.addAttribute("recommendation", recommendation);

        return "recommendations/view";
    }

    @PostMapping("/{id}/rate")
    public String rateRecommendation(@PathVariable Long id, @RequestParam Integer rating) {
        recommendationService.rateRecommendation(id, rating);
        return "redirect:/recommendations";
    }

    @GetMapping("/generate")
    public String generateRecommendations() {
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Get profile
        StudentProfile profile = profileService.getProfileByUser(user)
                .orElseThrow(() -> new IllegalStateException("Profile not found"));

        // Generate recommendations based on profile
        recommendationService.generateRecommendationsBasedOnProfile(user, profile);

        return "redirect:/recommendations";
    }
}