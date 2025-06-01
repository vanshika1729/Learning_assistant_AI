// AdminController.java
package com.learningassistant.controllers;

import com.learningassistant.models.User;
import com.learningassistant.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));

        model.addAttribute("user", user);
        return "admin/user-detail";
    }

    @GetMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/register-admin")
    public String registerAdminForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/register-admin";
    }

    @PostMapping("/register-admin")
    public String registerAdmin(@ModelAttribute User user) {
        userService.registerNewAdmin(user);
        return "redirect:/admin/users";
    }
}