package com.university.portal.controller;

import com.university.portal.dto.TeacherRequest;
import com.university.portal.entity.Teacher;
import com.university.portal.repository.TeacherRepository;
import com.university.portal.service.TeacherService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherWebController {

    private final TeacherService teacherService;
    private final TeacherRepository teacherRepository;

    public TeacherWebController(TeacherService teacherService, TeacherRepository teacherRepository) {
        this.teacherService = teacherService;
        this.teacherRepository = teacherRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        Teacher currentTeacher = teacherRepository.findByUserUsername(username).orElse(null);

        model.addAttribute("username", username);
        model.addAttribute("currentTeacher", currentTeacher);
        model.addAttribute("teachers", teacherService.getAllTeachers());

        return "teacher/dashboard";
    }

    @PostMapping("/teachers/create")
    public String createTeacher(@ModelAttribute TeacherRequest request, RedirectAttributes redirectAttributes) {
        try {
            teacherService.createTeacher(request);
            redirectAttributes.addFlashAttribute("successMessage", "Colleague faculty member successfully added.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add faculty: " + e.getMessage());
        }
        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/teachers/edit/{id}")
    public String editTeacher(@PathVariable Long id, @ModelAttribute TeacherRequest request, RedirectAttributes redirectAttributes) {
        try {
            teacherService.updateTeacher(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Faculty details successfully updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update faculty: " + e.getMessage());
        }
        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/teachers/delete/{id}")
    public String deleteTeacher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            teacherService.deleteTeacher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Faculty record removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete faculty: " + e.getMessage());
        }
        return "redirect:/teacher/dashboard";
    }
}
