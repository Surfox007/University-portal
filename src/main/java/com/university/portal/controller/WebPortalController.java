package com.university.portal.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebPortalController {

    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_ADMIN".equals(role)) {
                    return "redirect:/admin/dashboard";
                } else if ("ROLE_TEACHER".equals(role)) {
                    return "redirect:/teacher/dashboard";
                } else if ("ROLE_STUDENT".equals(role)) {
                    return "redirect:/student/dashboard";
                }
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Authentication authentication,
                            Model model) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/";
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password. Please verify your academic credentials.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out of the University Portal.");
        }
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Authentication authentication, Model model) {
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("authorities", authentication.getAuthorities());
        }
        return "access-denied";
    }
}
