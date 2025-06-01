// SubjectController.java
package com.learningassistant.controllers;

import com.learningassistant.models.Subject;
import com.learningassistant.models.Topic;
import com.learningassistant.services.SubjectService;
import com.learningassistant.services.TopicService;
import com.learningassistant.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listSubjects(Model model) {
        List<Subject> subjects = subjectService.getAllSubjects();
        model.addAttribute("subjects", subjects);
        return "subjects/list";
    }

    @GetMapping("/{id}")
    public String viewSubject(@PathVariable Long id, Model model) {
        Subject subject = subjectService.getSubjectById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid subject ID: " + id));

        List<Topic> topics = topicService.getTopicsBySubject(subject);

        model.addAttribute("subject", subject);
        model.addAttribute("topics", topics);

        return "subjects/view";
    }

    @GetMapping("/add")
    public String addSubjectForm(Model model) {
        model.addAttribute("subject", new Subject());
        return "subjects/add";
    }

    @PostMapping("/add")
    public String addSubject(@ModelAttribute Subject subject) {
        subjectService.createSubject(subject);
        return "redirect:/subjects";
    }

    @GetMapping("/{id}/edit")
    public String editSubjectForm(@PathVariable Long id, Model model) {
        Subject subject = subjectService.getSubjectById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid subject ID: " + id));

        model.addAttribute("subject", subject);
        return "subjects/edit";
    }

    @PostMapping("/{id}/edit")
    public String editSubject(@PathVariable Long id, @ModelAttribute Subject subject) {
        subject.setId(id);
        subjectService.updateSubject(subject);
        return "redirect:/subjects";
    }

    @GetMapping("/{id}/delete")
    public String deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return "redirect:/subjects";
    }
}