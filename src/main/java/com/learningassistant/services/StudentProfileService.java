// StudentProfileService.java
package com.learningassistant.services;

import com.learningassistant.models.StudentProfile;
import com.learningassistant.models.Subject;
import com.learningassistant.models.User;
import com.learningassistant.repositories.StudentProfileRepository;
import com.learningassistant.repositories.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class StudentProfileService {
    private final StudentProfileRepository profileRepository;
    private final SubjectRepository subjectRepository;

    @Autowired
    public StudentProfileService(StudentProfileRepository profileRepository,
                                 SubjectRepository subjectRepository) {
        this.profileRepository = profileRepository;
        this.subjectRepository = subjectRepository;
    }

    public Optional<StudentProfile> getProfileByUser(User user) {
        return profileRepository.findByUser(user);
    }

    @Transactional
    public StudentProfile updateProfile(User user, String educationLevel, String learningStyle) {
        StudentProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        profile.setEducationLevel(educationLevel);
        profile.setPreferredLearningStyle(learningStyle);

        return profileRepository.save(profile);
    }

    @Transactional
    public StudentProfile addSubjectInterest(User user, Long subjectId) {
        StudentProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        profile.getInterests().add(subject);
        return profileRepository.save(profile);
    }

    @Transactional
    public StudentProfile removeSubjectInterest(User user, Long subjectId) {
        StudentProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        profile.getInterests().remove(subject);
        return profileRepository.save(profile);
    }

    @Transactional
    public StudentProfile updateSkills(User user, Map<String, Integer> skills) {
        StudentProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        // Validate skill levels
        for (Integer level : skills.values()) {
            if (level < 1 || level > 10) {
                throw new IllegalArgumentException("Skill levels must be between 1 and 10");
            }
        }

        profile.setSkills(skills);
        return profileRepository.save(profile);
    }

    @Transactional
    public StudentProfile updateSkill(User user, String subject, int level) {
        if (level < 1 || level > 10) {
            throw new IllegalArgumentException("Skill level must be between 1 and 10");
        }

        StudentProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        Map<String, Integer> skills = profile.getSkills();
        skills.put(subject, level);
        profile.setSkills(skills);

        return profileRepository.save(profile);
    }

    public Set<Subject> getInterests(User user) {
        StudentProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        return profile.getInterests();
    }

    public Map<String, Integer> getSkills(User user) {
        StudentProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        return profile.getSkills();
    }
}
