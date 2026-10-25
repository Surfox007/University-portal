package com.university.portal.controller;

import com.university.portal.dto.StudentRequest;
import com.university.portal.entity.Student;
import com.university.portal.repository.StudentRepository;
import com.university.portal.service.NoticeService;
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
    private final NoticeService noticeService;

    public StudentWebController(StudentService studentService,
                                StudentRepository studentRepository,
                                NoticeService noticeService) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
        this.noticeService = noticeService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        Student currentStudent = studentRepository.findByUserUsername(username).orElse(null);

        model.addAttribute("username", username);
        model.addAttribute("currentStudent", currentStudent);
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("notices", noticeService.getAllNotices());

        return "student/dashboard";
    }

    @PostMapping("/notices/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam("content") String content,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            if (content == null || content.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Comment cannot be empty.");
                return "redirect:/student/dashboard#notice-" + id;
            }
            String username = authentication.getName();
            Student student = studentRepository.findByUserUsername(username).orElse(null);
            String authorName = (student != null) ? student.getFullName() : username;

            noticeService.addComment(id, authorName, "STUDENT", content.trim());
            redirectAttributes.addFlashAttribute("successMessage", "Comment posted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to post comment: " + e.getMessage());
        }
        return "redirect:/student/dashboard#notice-" + id;
    }

    @PostMapping("/students/create")
    public String createStudent(@ModelAttribute StudentRequest request, RedirectAttributes redirectAttributes) {
        try {
            studentService.createStudent(request);
            redirectAttributes.addFlashAttribute("successMessage", "Student record successfully added.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add student: " + e.getMessage());
        }
        return "redirect:/student/dashboard";
    }

    @PostMapping("/students/edit/{id}")
    public String editStudent(@PathVariable Long id, @ModelAttribute StudentRequest request, RedirectAttributes redirectAttributes) {
        try {
            studentService.updateStudent(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Profile details updated.");
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
