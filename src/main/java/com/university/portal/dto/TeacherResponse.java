package com.university.portal.dto;

public class TeacherResponse {
    private Long id;
    private String teacherCode;
    private String fullName;
    private String email;
    private String department;
    private String designation;
    private String phoneNumber;
    private String username;
    private String role;

    public TeacherResponse() {
    }

    public TeacherResponse(Long id, String teacherCode, String fullName, String email, String department, String designation, String phoneNumber, String username, String role) {
        this.id = id;
        this.teacherCode = teacherCode;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.designation = designation;
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

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
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

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
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
