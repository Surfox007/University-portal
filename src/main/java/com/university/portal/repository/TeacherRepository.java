package com.university.portal.repository;

import com.university.portal.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByTeacherCode(String teacherCode);
    Optional<Teacher> findByEmail(String email);
    Optional<Teacher> findByUserUsername(String username);
    Boolean existsByTeacherCode(String teacherCode);
    Boolean existsByEmail(String email);
}
