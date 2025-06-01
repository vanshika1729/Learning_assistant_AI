// ProfileController.java
package com.learningassistant.controllers;

import com.learningassistant.models.StudentProfile;
import com.learningassistant.models.Subject;
import com.learningassistant.models.User;
import com.learningassistant.services.StudentProfileService;
import com.learningassistant.services.SubjectService;
import com.learningassistant.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private StudentProfileService profileService;

    @Autowired
    private SubjectService subjectService;

    @GetMapping
    public String viewProfile(Model model) {
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Get profile
        StudentProfile profile = profileService.getProfileByUser(user)
                .orElseThrow(() -> new IllegalStateException("Profile not found"));

        // Get interests and skills
        Set<Subject> interests = profileService.getInterests(user);
        Map<String, Integer> skills = profileService.getSkills(user);

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("interests", interests);
        model.addAttribute("skills", skills);

        List<Subject> allSubjects = subjectService.getAllSubjects();
        model.addAttribute("allSubjects", allSubjects);

        return "profile/view";

    }

    @GetMapping("/edit")
    public String editProfileForm(Model model) {
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Get profile
        StudentProfile profile = profileService.getProfileByUser(user)
                .orElseThrow(() -> new IllegalStateException("Profile not found"));

        // Get all subjects for interests selection
        List<Subject> allSubjects = subjectService.getAllSubjects();

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("allSubjects", allSubjects);

        return "profile/edit";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String educationLevel,
                                @RequestParam String learningStyle) {
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Update user details
        user.setFirstName(firstName);
        user.setLastName(lastName);
        userService.updateUser(user);

        // Update profile
        profileService.updateProfile(user, educationLevel, learningStyle);

        return "redirect:/profile";
    }

    @PostMapping("/interests/add")
    public String addInterest(@RequestParam Long subjectId) {
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        profileService.addSubjectInterest(user, subjectId);

        return "redirect:/profile";
    }

    @GetMapping("/interests/remove/{subjectId}")
    public String removeInterest(@PathVariable Long subjectId) {
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        profileService.removeSubjectInterest(user, subjectId);

        return "redirect:/profile";
    }

    @PostMapping("/skills/update")
    public String updateSkill(@RequestParam String subject, @RequestParam Integer level) {
        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        profileService.updateSkill(user, subject, level);

        return "redirect:/profile";
    }
}
