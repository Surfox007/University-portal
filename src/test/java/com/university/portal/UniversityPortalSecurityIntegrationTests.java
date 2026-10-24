package com.university.portal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.portal.dto.AdminRequest;
import com.university.portal.dto.LoginRequest;
import com.university.portal.dto.StudentRequest;
import com.university.portal.dto.TeacherRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UniversityPortalSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getAuthToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("data").get("token").asText();
    }

    @Test
    @DisplayName("Login with valid Admin credentials should succeed and return JWT")
    void testAdminLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin", "admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.username", is("admin")))
                .andExpect(jsonPath("$.data.role", is("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Login with invalid credentials should return 401 Unauthorized")
    void testLoginWithWrongPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("Admin role can access Admin, Teacher, and Student resources")
    void testAdminCanAccessAllResources() throws Exception {
        String adminToken = getAuthToken("admin", "admin123");

        // Admin accessing Admin resource
        mockMvc.perform(get("/api/admins")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        // Admin accessing Teacher resource
        mockMvc.perform(get("/api/teachers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Admin accessing Student resource
        mockMvc.perform(get("/api/students")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Teacher role can manage Teachers, but is forbidden from Admins and Students")
    void testTeacherRolePermissions() throws Exception {
        String teacherToken = getAuthToken("teacher", "teacher123");

        // Teacher accessing Teacher resource -> OK
        mockMvc.perform(get("/api/teachers")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Teacher accessing Admin resource -> 403 Forbidden
        mockMvc.perform(get("/api/admins")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());

        // Teacher accessing Student resource -> 403 Forbidden
        mockMvc.perform(get("/api/students")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student role can manage Students, but is forbidden from Admins and Teachers")
    void testStudentRolePermissions() throws Exception {
        String studentToken = getAuthToken("student", "student123");

        // Student accessing Student resource -> OK
        mockMvc.perform(get("/api/students")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Student accessing Admin resource -> 403 Forbidden
        mockMvc.perform(get("/api/admins")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        // Student accessing Teacher resource -> 403 Forbidden
        mockMvc.perform(get("/api/teachers")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student CRUD lifecycle: Student can create, read, and update student")
    void testStudentCrud() throws Exception {
        String studentToken = getAuthToken("student", "student123");

        StudentRequest request = new StudentRequest();
        request.setStudentId("STU-TEST-999");
        request.setFullName("John Doe");
        request.setEmail("johndoe.test@university.edu");
        request.setDepartment("Computer Science");
        request.setSemester("2nd Semester");
        request.setCgpa(3.75);
        request.setPhoneNumber("+1234567890");
        request.setUsername("johndoetest");
        request.setPassword("password123");

        // Create student
        MvcResult createResult = mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.studentId", is("STU-TEST-999")))
                .andReturn();

        Long createdId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        // Read student by id
        mockMvc.perform(get("/api/students/" + createdId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName", is("John Doe")));

        // Update student
        request.setFullName("John Updated");
        mockMvc.perform(put("/api/students/" + createdId)
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName", is("John Updated")));

        // Delete student
        mockMvc.perform(delete("/api/students/" + createdId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }
}
