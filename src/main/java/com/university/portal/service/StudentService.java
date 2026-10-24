package com.university.portal.service;

import com.university.portal.dto.StudentRequest;
import com.university.portal.dto.StudentResponse;
import com.university.portal.entity.Role;
import com.university.portal.entity.Student;
import com.university.portal.entity.User;
import com.university.portal.exception.BadRequestException;
import com.university.portal.exception.ResourceNotFoundException;
import com.university.portal.repository.StudentRepository;
import com.university.portal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return mapToResponse(student);
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new BadRequestException("Student ID is already registered: " + request.getStudentId());
        }

        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Student email is already registered: " + request.getEmail());
        }

        String username = (request.getUsername() != null && !request.getUsername().isBlank())
                ? request.getUsername()
                : request.getStudentId().toLowerCase().replaceAll("[^a-z0-9]", "");

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists: " + username);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User email already exists: " + request.getEmail());
        }

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : "student123";

        User user = new User(
                username,
                passwordEncoder.encode(rawPassword),
                request.getEmail(),
                Role.ROLE_STUDENT
        );

        Student student = new Student(
                request.getStudentId(),
                request.getFullName(),
                request.getEmail(),
                request.getDepartment(),
                request.getSemester(),
                request.getCgpa(),
                request.getPhoneNumber(),
                user
        );

        Student savedStudent = studentRepository.save(student);
        return mapToResponse(savedStudent);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (!student.getStudentId().equals(request.getStudentId()) &&
                studentRepository.existsByStudentId(request.getStudentId())) {
            throw new BadRequestException("Student ID is already registered: " + request.getStudentId());
        }

        if (!student.getEmail().equalsIgnoreCase(request.getEmail()) &&
                studentRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Student email is already taken: " + request.getEmail());
        }

        student.setStudentId(request.getStudentId());
        student.setFullName(request.getFullName());
        student.setEmail(request.getEmail());
        student.setDepartment(request.getDepartment());
        student.setSemester(request.getSemester());
        student.setCgpa(request.getCgpa());
        student.setPhoneNumber(request.getPhoneNumber());

        User user = student.getUser();
        if (user != null) {
            user.setEmail(request.getEmail());
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        }

        Student updatedStudent = studentRepository.save(student);
        return mapToResponse(updatedStudent);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        studentRepository.delete(student);
    }

    private StudentResponse mapToResponse(Student student) {
        String username = student.getUser() != null ? student.getUser().getUsername() : null;
        String role = student.getUser() != null ? student.getUser().getRole().name() : Role.ROLE_STUDENT.name();

        return new StudentResponse(
                student.getId(),
                student.getStudentId(),
                student.getFullName(),
                student.getEmail(),
                student.getDepartment(),
                student.getSemester(),
                student.getCgpa(),
                student.getPhoneNumber(),
                username,
                role
        );
    }
}
