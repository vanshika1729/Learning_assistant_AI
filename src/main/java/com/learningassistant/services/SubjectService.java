// SubjectService.java
package com.learningassistant.services;

import com.learningassistant.models.Subject;
import com.learningassistant.repositories.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepository;

    @Autowired
    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Optional<Subject> getSubjectById(Long id) {
        return subjectRepository.findById(id);
    }

    public Optional<Subject> getSubjectByName(String name) {
        return subjectRepository.findByName(name);
    }

    @Transactional
    public Subject createSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    @Transactional
    public Subject updateSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    @Transactional
    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }
}

