package com.university.portal.config;

import com.university.portal.entity.*;
import com.university.portal.repository.AdminRepository;
import com.university.portal.repository.StudentRepository;
import com.university.portal.repository.TeacherRepository;
import com.university.portal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           AdminRepository adminRepository,
                           TeacherRepository teacherRepository,
                           StudentRepository studentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            logger.info("Initializing database with default Admin, Teacher, and Student credentials...");

            // 1. Seed Admin
            User adminUser = new User("admin", passwordEncoder.encode("admin123"), "admin@university.edu", Role.ROLE_ADMIN);
            Admin admin = new Admin(
                    "ADM-001",
                    "System Administrator",
                    "admin@university.edu",
                    "Central Administration",
                    "+1-555-0101",
                    adminUser
            );
            adminRepository.save(admin);
            logger.info("Default Admin created: username='admin', password='admin123'");

            // 2. Seed Teacher
            User teacherUser = new User("teacher", passwordEncoder.encode("teacher123"), "teacher@university.edu", Role.ROLE_TEACHER);
            Teacher teacher = new Teacher(
                    "T-101",
                    "Dr. Alan Turing",
                    "teacher@university.edu",
                    "Computer Science & Engineering",
                    "Professor",
                    "+1-555-0102",
                    teacherUser
            );
            teacherRepository.save(teacher);
            logger.info("Default Teacher created: username='teacher', password='teacher123'");

            // 3. Seed Student
            User studentUser = new User("student", passwordEncoder.encode("student123"), "student@university.edu", Role.ROLE_STUDENT);
            Student student = new Student(
                    "STU-2024-001",
                    "Ada Lovelace",
                    "student@university.edu",
                    "Computer Science & Engineering",
                    "4th Semester",
                    3.95,
                    "+1-555-0103",
                    studentUser
            );
            studentRepository.save(student);
            logger.info("Default Student created: username='student', password='student123'");

            logger.info("Database seeding completed successfully!");
        } else {
            logger.info("Users already exist in database. Skipping data initialization.");
        }
    }
}
