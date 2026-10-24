package com.university.portal.repository;

import com.university.portal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByEmail(String email);
    Optional<Student> findByUserUsername(String username);
    Boolean existsByStudentId(String studentId);
    Boolean existsByEmail(String email);
}
