package com.university.portal.config;

import com.university.portal.entity.*;
import com.university.portal.repository.AdminRepository;
import com.university.portal.repository.NoticeRepository;
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
    private final NoticeRepository noticeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           AdminRepository adminRepository,
                           TeacherRepository teacherRepository,
                           StudentRepository studentRepository,
                           NoticeRepository noticeRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.noticeRepository = noticeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            logger.info("Initializing database with authentic university demo records...");

            // 1. Exactly ONE Admin
            User adminUser = new User("admin", passwordEncoder.encode("admin123"), "admin@university.edu", Role.ROLE_ADMIN);
            Admin admin = new Admin(
                    "ADM-001",
                    "Dr. Robert Vance",
                    "admin@university.edu",
                    "Office of the Registrar",
                    "+1-555-0100",
                    adminUser
            );
            adminRepository.save(admin);
            logger.info("Admin created: username='admin', password='admin123'");

            // 2. Demo Teachers
            Teacher t1 = new Teacher(
                    "T-101",
                    "Dr. Alan Turing",
                    "teacher@university.edu",
                    "Computer Science & Engineering",
                    "Professor",
                    "+1-555-0101",
                    new User("teacher", passwordEncoder.encode("teacher123"), "teacher@university.edu", Role.ROLE_TEACHER)
            );
            teacherRepository.save(t1);

            Teacher t2 = new Teacher(
                    "T-102",
                    "Prof. Margaret Hamilton",
                    "margaret@university.edu",
                    "Software Engineering",
                    "Associate Professor",
                    "+1-555-0102",
                    new User("margaret", passwordEncoder.encode("teacher123"), "margaret@university.edu", Role.ROLE_TEACHER)
            );
            teacherRepository.save(t2);

            Teacher t3 = new Teacher(
                    "T-103",
                    "Dr. Donald Knuth",
                    "knuth@university.edu",
                    "Computer Science",
                    "Professor",
                    "+1-555-0103",
                    new User("knuth", passwordEncoder.encode("teacher123"), "knuth@university.edu", Role.ROLE_TEACHER)
            );
            teacherRepository.save(t3);

            Teacher t4 = new Teacher(
                    "T-104",
                    "Dr. Grace Hopper",
                    "hopper@university.edu",
                    "Information Technology",
                    "Assistant Professor",
                    "+1-555-0104",
                    new User("hopper", passwordEncoder.encode("teacher123"), "hopper@university.edu", Role.ROLE_TEACHER)
            );
            teacherRepository.save(t4);
            logger.info("Demo Teachers created: Alan Turing, Margaret Hamilton, Donald Knuth, Grace Hopper");

            // 3. Demo Students
            Student s1 = new Student(
                    "STU-2024-001",
                    "Ada Lovelace",
                    "student@university.edu",
                    "Computer Science & Engineering",
                    "4th Semester",
                    3.95,
                    "+1-555-0201",
                    new User("student", passwordEncoder.encode("student123"), "student@university.edu", Role.ROLE_STUDENT)
            );
            studentRepository.save(s1);

            Student s2 = new Student(
                    "STU-2023-014",
                    "Alan Kay",
                    "alankay@university.edu",
                    "Software Engineering",
                    "6th Semester",
                    3.82,
                    "+1-555-0202",
                    new User("alankay", passwordEncoder.encode("student123"), "alankay@university.edu", Role.ROLE_STUDENT)
            );
            studentRepository.save(s2);

            Student s3 = new Student(
                    "STU-2023-088",
                    "Claude Shannon",
                    "shannon@university.edu",
                    "Electrical & Electronic Engineering",
                    "5th Semester",
                    3.91,
                    "+1-555-0203",
                    new User("shannon", passwordEncoder.encode("student123"), "shannon@university.edu", Role.ROLE_STUDENT)
            );
            studentRepository.save(s3);

            Student s4 = new Student(
                    "STU-2024-042",
                    "Barbara Liskov",
                    "barbara@university.edu",
                    "Computer Science",
                    "3rd Semester",
                    3.88,
                    "+1-555-0204",
                    new User("barbara", passwordEncoder.encode("student123"), "barbara@university.edu", Role.ROLE_STUDENT)
            );
            studentRepository.save(s4);

            Student s5 = new Student(
                    "STU-2022-019",
                    "Linus Torvalds",
                    "linus@university.edu",
                    "Computer Systems",
                    "7th Semester",
                    3.75,
                    "+1-555-0205",
                    new User("linus", passwordEncoder.encode("student123"), "linus@university.edu", Role.ROLE_STUDENT)
            );
            studentRepository.save(s5);
            logger.info("Demo Students created: Ada Lovelace, Alan Kay, Claude Shannon, Barbara Liskov, Linus Torvalds");

            // 4. Campus Notices with Student Questions & Comments
            Notice n1 = new Notice(
                    "Midterm Examination Timetable - Spring 2026",
                    "The Midterm Examinations for the Spring 2026 semester will take place from November 15, 2026 to November 25, 2026. Please check your personalized course schedule and room allocations on the department board. Review lectures will be conducted next week during scheduled class hours.",
                    "Examination",
                    "Dr. Alan Turing",
                    "TEACHER"
            );
            n1.addComment(new NoticeComment("Ada Lovelace", "STUDENT", "Will the review session slide decks be uploaded beforehand?", n1));
            n1.addComment(new NoticeComment("Dr. Alan Turing", "TEACHER", "Yes Ada, all review slides and practice questions will be uploaded by Friday.", n1));
            noticeRepository.save(n1);

            Notice n2 = new Notice(
                    "Software Engineering Project Phase 1 Submission Deadline",
                    "All student teams must submit their System Requirement Specification (SRS) documentation and conceptual architecture diagrams via the portal by Sunday, 11:59 PM. Group presentations will follow during laboratory hours next Tuesday.",
                    "Academic",
                    "Prof. Margaret Hamilton",
                    "TEACHER"
            );
            n2.addComment(new NoticeComment("Alan Kay", "STUDENT", "Could you confirm if the component interaction diagrams are mandatory for Phase 1 or Phase 2?", n2));
            n2.addComment(new NoticeComment("Prof. Margaret Hamilton", "TEACHER", "Component interaction diagrams are part of Phase 2. Focus on SRS and domain use cases for this milestone.", n2));
            noticeRepository.save(n2);

            Notice n3 = new Notice(
                    "Central Campus Library - 24/7 Extended Study Hours",
                    "Starting next Monday, the Central Library will remain open 24/7 to support students preparing for upcoming midterm assessments. High-speed campus Wi-Fi, reference desks, and quiet study zones will be accessible around the clock with your university student ID card.",
                    "General",
                    "Dr. Robert Vance",
                    "ADMIN"
            );
            n3.addComment(new NoticeComment("Linus Torvalds", "STUDENT", "Are the collaborative project discussion rooms also reservable during overnight hours?", n3));
            n3.addComment(new NoticeComment("Dr. Robert Vance", "ADMIN", "Yes, discussion room reservations can be requested through the digital circulation desk.", n3));
            noticeRepository.save(n3);

            logger.info("Database seeding completed successfully with demo records and notices!");
        } else {
            logger.info("Users already exist in database. Skipping data initialization.");
        }
    }
}
