package com.university.portal.controller;

import com.university.portal.dto.TeacherRequest;
import com.university.portal.entity.Teacher;
import com.university.portal.repository.TeacherRepository;
import com.university.portal.service.AuthService;
import com.university.portal.service.NoticeService;
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
    private final NoticeService noticeService;
    private final AuthService authService;

    public TeacherWebController(TeacherService teacherService,
                                TeacherRepository teacherRepository,
                                NoticeService noticeService,
                                AuthService authService) {
        this.teacherService = teacherService;
        this.teacherRepository = teacherRepository;
        this.noticeService = noticeService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        Teacher currentTeacher = teacherRepository.findByUserUsername(username).orElse(null);

        model.addAttribute("username", username);
        model.addAttribute("currentTeacher", currentTeacher);
        model.addAttribute("teachers", teacherService.getAllTeachers());
        model.addAttribute("notices", noticeService.getAllNotices());

        return "teacher/dashboard";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            authService.changePassword(authentication.getName(), currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password successfully updated! Please remember your new password.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/notices/create")
    public String createNotice(@RequestParam("title") String title,
                               @RequestParam("category") String category,
                               @RequestParam("content") String content,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            Teacher teacher = teacherRepository.findByUserUsername(username).orElse(null);
            String authorName = (teacher != null) ? teacher.getFullName() : username;

            noticeService.createNotice(title.trim(), content.trim(), category, authorName, "TEACHER");
            redirectAttributes.addFlashAttribute("successMessage", "Notice successfully published to students.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to publish notice: " + e.getMessage());
        }
        return "redirect:/teacher/dashboard#notices";
    }

    @PostMapping("/notices/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam("content") String content,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            if (content == null || content.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Reply cannot be empty.");
                return "redirect:/teacher/dashboard#notice-" + id;
            }
            String username = authentication.getName();
            Teacher teacher = teacherRepository.findByUserUsername(username).orElse(null);
            String authorName = (teacher != null) ? teacher.getFullName() : username;

            noticeService.addComment(id, authorName, "TEACHER", content.trim());
            redirectAttributes.addFlashAttribute("successMessage", "Reply posted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to post reply: " + e.getMessage());
        }
        return "redirect:/teacher/dashboard#notice-" + id;
    }

    @PostMapping("/notices/delete/{id}")
    public String deleteNotice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            noticeService.deleteNotice(id);
            redirectAttributes.addFlashAttribute("successMessage", "Notice deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete notice: " + e.getMessage());
        }
        return "redirect:/teacher/dashboard#notices";
    }

    @PostMapping("/teachers/create")
    public String createTeacher(@ModelAttribute TeacherRequest request, RedirectAttributes redirectAttributes) {
        try {
            teacherService.createTeacher(request);
            redirectAttributes.addFlashAttribute("successMessage", "Faculty member successfully added.");
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
