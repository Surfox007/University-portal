package com.university.portal.controller;

import com.university.portal.dto.AdminRequest;
import com.university.portal.dto.StudentRequest;
import com.university.portal.dto.TeacherRequest;
import com.university.portal.service.AdminService;
import com.university.portal.service.StudentService;
import com.university.portal.service.TeacherService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWebController {

    private final AdminService adminService;
    private final TeacherService teacherService;
    private final StudentService studentService;

    public AdminWebController(AdminService adminService,
                              TeacherService teacherService,
                              StudentService studentService) {
        this.adminService = adminService;
        this.teacherService = teacherService;
        this.studentService = studentService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(value = "tab", defaultValue = "overview") String activeTab,
                            Authentication authentication,
                            Model model) {
        model.addAttribute("adminUser", authentication.getName());
        model.addAttribute("activeTab", activeTab);

        model.addAttribute("admins", adminService.getAllAdmins());
        model.addAttribute("teachers", teacherService.getAllTeachers());
        model.addAttribute("students", studentService.getAllStudents());

        model.addAttribute("totalAdmins", adminService.getAllAdmins().size());
        model.addAttribute("totalTeachers", teacherService.getAllTeachers().size());
        model.addAttribute("totalStudents", studentService.getAllStudents().size());

        return "admin/dashboard";
    }

    // --- Student Management Actions ---
    @PostMapping("/students/create")
    public String createStudent(@ModelAttribute StudentRequest request, RedirectAttributes redirectAttributes) {
        try {
            studentService.createStudent(request);
            redirectAttributes.addFlashAttribute("successMessage", "Student record successfully added.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add student: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=students";
    }

    @PostMapping("/students/edit/{id}")
    public String editStudent(@PathVariable Long id, @ModelAttribute StudentRequest request, RedirectAttributes redirectAttributes) {
        try {
            studentService.updateStudent(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Student record successfully updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update student: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=students";
    }

    @PostMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student record removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete student: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=students";
    }

    // --- Teacher Management Actions ---
    @PostMapping("/teachers/create")
    public String createTeacher(@ModelAttribute TeacherRequest request, RedirectAttributes redirectAttributes) {
        try {
            teacherService.createTeacher(request);
            redirectAttributes.addFlashAttribute("successMessage", "Faculty member successfully registered.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add faculty: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=teachers";
    }

    @PostMapping("/teachers/edit/{id}")
    public String editTeacher(@PathVariable Long id, @ModelAttribute TeacherRequest request, RedirectAttributes redirectAttributes) {
        try {
            teacherService.updateTeacher(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Faculty record successfully updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update faculty: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=teachers";
    }

    @PostMapping("/teachers/delete/{id}")
    public String deleteTeacher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            teacherService.deleteTeacher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Faculty record removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete faculty: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=teachers";
    }

    // --- Admin Management Actions ---
    @PostMapping("/admins/create")
    public String createAdmin(@ModelAttribute AdminRequest request, RedirectAttributes redirectAttributes) {
        try {
            adminService.createAdmin(request);
            redirectAttributes.addFlashAttribute("successMessage", "Administrator successfully created.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add administrator: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=admins";
    }

    @PostMapping("/admins/edit/{id}")
    public String editAdmin(@PathVariable Long id, @ModelAttribute AdminRequest request, RedirectAttributes redirectAttributes) {
        try {
            adminService.updateAdmin(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Administrator record updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update administrator: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=admins";
    }

    @PostMapping("/admins/delete/{id}")
    public String deleteAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteAdmin(id);
            redirectAttributes.addFlashAttribute("successMessage", "Administrator record removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete administrator: " + e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=admins";
    }
}
