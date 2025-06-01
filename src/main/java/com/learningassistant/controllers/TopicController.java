// TopicController.java
package com.learningassistant.controllers;

import com.learningassistant.models.Subject;
import com.learningassistant.models.Topic;
import com.learningassistant.services.SubjectService;
import com.learningassistant.services.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/topics")
public class TopicController {

    @Autowired
    private TopicService topicService;

    @Autowired
    private SubjectService subjectService;

    @GetMapping("/{id}")
    public String viewTopic(@PathVariable Long id, Model model) {
        Topic topic = topicService.getTopicById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid topic ID: " + id));

        model.addAttribute("topic", topic);
        return "topics/view";
    }

    @GetMapping("/add")
    public String addTopicForm(Model model) {
        model.addAttribute("topic", new Topic());
        model.addAttribute("subjects", subjectService.getAllSubjects());
        return "topics/add";
    }

    @PostMapping("/add")
    public String addTopic(@ModelAttribute Topic topic, @RequestParam Long subjectId) {
        Subject subject = subjectService.getSubjectById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid subject ID: " + subjectId));

        topic.setSubject(subject);
        topicService.createTopic(topic);
        return "redirect:/subjects/" + subjectId;
    }

    @GetMapping("/{id}/edit")
    public String editTopicForm(@PathVariable Long id, Model model) {
        Topic topic = topicService.getTopicById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid topic ID: " + id));

        model.addAttribute("topic", topic);
        model.addAttribute("subjects", subjectService.getAllSubjects());
        return "topics/edit";
    }

    @PostMapping("/{id}/edit")
    public String editTopic(@PathVariable Long id, @ModelAttribute Topic topic, @RequestParam Long subjectId) {
        Subject subject = subjectService.getSubjectById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid subject ID: " + subjectId));

        topic.setId(id);
        topic.setSubject(subject);
        topicService.updateTopic(topic);
        return "redirect:/subjects/" + subjectId;
    }

    @GetMapping("/{id}/delete")
    public String deleteTopic(@PathVariable Long id) {
        Topic topic = topicService.getTopicById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid topic ID: " + id));

        Long subjectId = topic.getSubject().getId();
        topicService.deleteTopic(id);
        return "redirect:/subjects/" + subjectId;
    }
}
