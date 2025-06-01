// LearningMaterialController.java
package com.learningassistant.controllers;

import com.learningassistant.models.LearningMaterial;
import com.learningassistant.models.Topic;
import com.learningassistant.services.LearningMaterialService;
import com.learningassistant.services.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/materials")
public class LearningMaterialController {

    @Autowired
    private LearningMaterialService materialService;

    @Autowired
    private TopicService topicService;

    @GetMapping("")  // This will handle the root /materials path
    public String listAllMaterials(Model model) {
        model.addAttribute("materials", materialService.getAllMaterials());
        return "materials/list";
    }

    @GetMapping("/{id}")
    public String viewMaterial(@PathVariable Long id, Model model) {
        LearningMaterial material = materialService.getMaterialById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid material ID: " + id));

        model.addAttribute("material", material);
        return "materials/view";
    }

    @GetMapping("/add")
    public String addMaterialForm(Model model) {
        model.addAttribute("material", new LearningMaterial());
        model.addAttribute("topics", topicService.getAllTopics());
        model.addAttribute("materialTypes", LearningMaterial.MaterialType.values());
        return "materials/add";
    }

    @PostMapping("/add")
    public String addMaterial(@ModelAttribute LearningMaterial material, @RequestParam Long topicId) {
        Topic topic = topicService.getTopicById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid topic ID: " + topicId));

        material.setTopic(topic);
        materialService.createMaterial(material);
        return "redirect:/topics/" + topicId;
    }

    @GetMapping("/{id}/edit")
    public String editMaterialForm(@PathVariable Long id, Model model) {
        LearningMaterial material = materialService.getMaterialById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid material ID: " + id));

        model.addAttribute("material", material);
        model.addAttribute("topics", topicService.getAllTopics());
        model.addAttribute("materialTypes", LearningMaterial.MaterialType.values());
        return "materials/edit";
    }

    @PostMapping("/{id}/edit")
    public String editMaterial(@PathVariable Long id, @ModelAttribute LearningMaterial material, @RequestParam Long topicId) {
        Topic topic = topicService.getTopicById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid topic ID: " + topicId));

        material.setId(id);
        material.setTopic(topic);
        materialService.updateMaterial(material);
        return "redirect:/topics/" + topicId;
    }

    @GetMapping("/{id}/delete")
    public String deleteMaterial(@PathVariable Long id) {
        LearningMaterial material = materialService.getMaterialById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid material ID: " + id));

        Long topicId = material.getTopic().getId();
        materialService.deleteMaterial(id);
        return "redirect:/topics/" + topicId;
    }
}