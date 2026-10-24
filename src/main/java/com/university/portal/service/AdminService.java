package com.university.portal.service;

import com.university.portal.dto.AdminRequest;
import com.university.portal.dto.AdminResponse;
import com.university.portal.entity.Admin;
import com.university.portal.entity.Role;
import com.university.portal.entity.User;
import com.university.portal.exception.BadRequestException;
import com.university.portal.exception.ResourceNotFoundException;
import com.university.portal.repository.AdminRepository;
import com.university.portal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminResponse getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
        return mapToResponse(admin);
    }

    @Transactional
    public AdminResponse createAdmin(AdminRequest request) {
        if (adminRepository.existsByAdminCode(request.getAdminCode())) {
            throw new BadRequestException("Admin code is already taken: " + request.getAdminCode());
        }

        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Admin email is already registered: " + request.getEmail());
        }

        String username = (request.getUsername() != null && !request.getUsername().isBlank())
                ? request.getUsername()
                : request.getAdminCode().toLowerCase().replaceAll("[^a-z0-9]", "");

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists: " + username);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User email already exists: " + request.getEmail());
        }

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : "admin123";

        User user = new User(
                username,
                passwordEncoder.encode(rawPassword),
                request.getEmail(),
                Role.ROLE_ADMIN
        );

        Admin admin = new Admin(
                request.getAdminCode(),
                request.getFullName(),
                request.getEmail(),
                request.getDepartment(),
                request.getPhoneNumber(),
                user
        );

        Admin savedAdmin = adminRepository.save(admin);
        return mapToResponse(savedAdmin);
    }

    @Transactional
    public AdminResponse updateAdmin(Long id, AdminRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        // Check if adminCode changed and already exists
        if (!admin.getAdminCode().equals(request.getAdminCode()) &&
                adminRepository.existsByAdminCode(request.getAdminCode())) {
            throw new BadRequestException("Admin code is already taken: " + request.getAdminCode());
        }

        // Check if email changed and already exists
        if (!admin.getEmail().equalsIgnoreCase(request.getEmail()) &&
                adminRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Admin email is already taken: " + request.getEmail());
        }

        admin.setAdminCode(request.getAdminCode());
        admin.setFullName(request.getFullName());
        admin.setEmail(request.getEmail());
        admin.setDepartment(request.getDepartment());
        admin.setPhoneNumber(request.getPhoneNumber());

        // Update user email and optionally password
        User user = admin.getUser();
        if (user != null) {
            user.setEmail(request.getEmail());
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        }

        Admin updatedAdmin = adminRepository.save(admin);
        return mapToResponse(updatedAdmin);
    }

    @Transactional
    public void deleteAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
        adminRepository.delete(admin);
    }

    private AdminResponse mapToResponse(Admin admin) {
        String username = admin.getUser() != null ? admin.getUser().getUsername() : null;
        String role = admin.getUser() != null ? admin.getUser().getRole().name() : Role.ROLE_ADMIN.name();

        return new AdminResponse(
                admin.getId(),
                admin.getAdminCode(),
                admin.getFullName(),
                admin.getEmail(),
                admin.getDepartment(),
                admin.getPhoneNumber(),
                username,
                role
        );
    }
}
