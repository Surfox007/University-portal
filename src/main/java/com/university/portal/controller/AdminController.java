package com.university.portal.controller;

import com.university.portal.dto.AdminRequest;
import com.university.portal.dto.AdminResponse;
import com.university.portal.dto.ApiResponse;
import com.university.portal.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminResponse>>> getAllAdmins() {
        List<AdminResponse> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(ApiResponse.success("Admins retrieved successfully", admins));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminResponse>> getAdminById(@PathVariable Long id) {
        AdminResponse admin = adminService.getAdminById(id);
        return ResponseEntity.ok(ApiResponse.success("Admin retrieved successfully", admin));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminResponse>> createAdmin(@Valid @RequestBody AdminRequest request) {
        AdminResponse createdAdmin = adminService.createAdmin(request);
        return new ResponseEntity<>(
                ApiResponse.success("Admin resource created successfully", createdAdmin),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminResponse>> updateAdmin(@PathVariable Long id,
                                                                 @Valid @RequestBody AdminRequest request) {
        AdminResponse updatedAdmin = adminService.updateAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success("Admin resource updated successfully", updatedAdmin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Admin resource deleted successfully", null));
    }
}
