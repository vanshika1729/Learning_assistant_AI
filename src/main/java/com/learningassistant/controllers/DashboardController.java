// DashboardController.java
package com.learningassistant.controllers;

import com.learningassistant.models.*;
import com.learningassistant.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private StudentProfileService profileService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Add user to model
        model.addAttribute("user", user);

        // Get recent quiz attempts
        List<QuizAttempt> recentAttempts = quizService.getUserAttempts(user);
        model.addAttribute("recentAttempts", recentAttempts.stream().limit(5).toList());

        // Get new recommendations
        List<Recommendation> newRecommendations = recommendationService.getNewRecommendations(user);
        model.addAttribute("recommendations", newRecommendations);

        // Get all subjects
        List<Subject> allSubjects = subjectService.getAllSubjects();
        model.addAttribute("subjects", allSubjects);

        // Get user's interests
        model.addAttribute("interests", profileService.getInterests(user));

        return "dashboard";
    }
}