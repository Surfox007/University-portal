package com.university.portal.controller;

import com.university.portal.dto.StudentRequest;
import com.university.portal.entity.Student;
import com.university.portal.repository.StudentRepository;
import com.university.portal.service.StudentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentWebController {

    private final StudentService studentService;
    private final StudentRepository studentRepository;

    public StudentWebController(StudentService studentService, StudentRepository studentRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        Student currentStudent = studentRepository.findByUserUsername(username).orElse(null);

        model.addAttribute("username", username);
        model.addAttribute("currentStudent", currentStudent);
        model.addAttribute("students", studentService.getAllStudents());

        return "student/dashboard";
    }

    @PostMapping("/students/create")
    public String createStudent(@ModelAttribute StudentRequest request, RedirectAttributes redirectAttributes) {
        try {
            studentService.createStudent(request);
            redirectAttributes.addFlashAttribute("successMessage", "Student record successfully submitted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit student: " + e.getMessage());
        }
        return "redirect:/student/dashboard";
    }

    @PostMapping("/students/edit/{id}")
    public String editStudent(@PathVariable Long id, @ModelAttribute StudentRequest request, RedirectAttributes redirectAttributes) {
        try {
            studentService.updateStudent(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Profile / Student details updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update record: " + e.getMessage());
        }
        return "redirect:/student/dashboard";
    }

    @PostMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student record removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete record: " + e.getMessage());
        }
        return "redirect:/student/dashboard";
    }
}
