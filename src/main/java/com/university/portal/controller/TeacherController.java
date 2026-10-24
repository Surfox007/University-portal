package com.university.portal.controller;

import com.university.portal.dto.ApiResponse;
import com.university.portal.dto.TeacherRequest;
import com.university.portal.dto.TeacherResponse;
import com.university.portal.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getAllTeachers() {
        List<TeacherResponse> teachers = teacherService.getAllTeachers();
        return ResponseEntity.ok(ApiResponse.success("Teachers retrieved successfully", teachers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherById(@PathVariable Long id) {
        TeacherResponse teacher = teacherService.getTeacherById(id);
        return ResponseEntity.ok(ApiResponse.success("Teacher retrieved successfully", teacher));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(@Valid @RequestBody TeacherRequest request) {
        TeacherResponse createdTeacher = teacherService.createTeacher(request);
        return new ResponseEntity<>(
                ApiResponse.success("Teacher resource created successfully", createdTeacher),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacher(@PathVariable Long id,
                                                                     @Valid @RequestBody TeacherRequest request) {
        TeacherResponse updatedTeacher = teacherService.updateTeacher(id, request);
        return ResponseEntity.ok(ApiResponse.success("Teacher resource updated successfully", updatedTeacher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.success("Teacher resource deleted successfully", null));
    }
}
