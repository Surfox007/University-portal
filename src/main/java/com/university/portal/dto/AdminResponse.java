package com.university.portal.dto;

public class AdminResponse {
    private Long id;
    private String adminCode;
    private String fullName;
    private String email;
    private String department;
    private String phoneNumber;
    private String username;
    private String role;

    public AdminResponse() {
    }

    public AdminResponse(Long id, String adminCode, String fullName, String email, String department, String phoneNumber, String username, String role) {
        this.id = id;
        this.adminCode = adminCode;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
