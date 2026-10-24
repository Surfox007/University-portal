package com.university.portal.service;

import com.university.portal.dto.TeacherRequest;
import com.university.portal.dto.TeacherResponse;
import com.university.portal.entity.Role;
import com.university.portal.entity.Teacher;
import com.university.portal.entity.User;
import com.university.portal.exception.BadRequestException;
import com.university.portal.exception.ResourceNotFoundException;
import com.university.portal.repository.TeacherRepository;
import com.university.portal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherService(TeacherRepository teacherRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
        return mapToResponse(teacher);
    }

    @Transactional
    public TeacherResponse createTeacher(TeacherRequest request) {
        if (teacherRepository.existsByTeacherCode(request.getTeacherCode())) {
            throw new BadRequestException("Teacher code is already taken: " + request.getTeacherCode());
        }

        if (teacherRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Teacher email is already registered: " + request.getEmail());
        }

        String username = (request.getUsername() != null && !request.getUsername().isBlank())
                ? request.getUsername()
                : request.getTeacherCode().toLowerCase().replaceAll("[^a-z0-9]", "");

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists: " + username);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User email already exists: " + request.getEmail());
        }

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : "teacher123";

        User user = new User(
                username,
                passwordEncoder.encode(rawPassword),
                request.getEmail(),
                Role.ROLE_TEACHER
        );

        Teacher teacher = new Teacher(
                request.getTeacherCode(),
                request.getFullName(),
                request.getEmail(),
                request.getDepartment(),
                request.getDesignation(),
                request.getPhoneNumber(),
                user
        );

        Teacher savedTeacher = teacherRepository.save(teacher);
        return mapToResponse(savedTeacher);
    }

    @Transactional
    public TeacherResponse updateTeacher(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        if (!teacher.getTeacherCode().equals(request.getTeacherCode()) &&
                teacherRepository.existsByTeacherCode(request.getTeacherCode())) {
            throw new BadRequestException("Teacher code is already taken: " + request.getTeacherCode());
        }

        if (!teacher.getEmail().equalsIgnoreCase(request.getEmail()) &&
                teacherRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Teacher email is already taken: " + request.getEmail());
        }

        teacher.setTeacherCode(request.getTeacherCode());
        teacher.setFullName(request.getFullName());
        teacher.setEmail(request.getEmail());
        teacher.setDepartment(request.getDepartment());
        teacher.setDesignation(request.getDesignation());
        teacher.setPhoneNumber(request.getPhoneNumber());

        User user = teacher.getUser();
        if (user != null) {
            user.setEmail(request.getEmail());
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        }

        Teacher updatedTeacher = teacherRepository.save(teacher);
        return mapToResponse(updatedTeacher);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
        teacherRepository.delete(teacher);
    }

    private TeacherResponse mapToResponse(Teacher teacher) {
        String username = teacher.getUser() != null ? teacher.getUser().getUsername() : null;
        String role = teacher.getUser() != null ? teacher.getUser().getRole().name() : Role.ROLE_TEACHER.name();

        return new TeacherResponse(
                teacher.getId(),
                teacher.getTeacherCode(),
                teacher.getFullName(),
                teacher.getEmail(),
                teacher.getDepartment(),
                teacher.getDesignation(),
                teacher.getPhoneNumber(),
                username,
                role
        );
    }
}
