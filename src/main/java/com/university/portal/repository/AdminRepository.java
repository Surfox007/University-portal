package com.university.portal.repository;

import com.university.portal.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByAdminCode(String adminCode);
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByUserUsername(String username);
    Boolean existsByAdminCode(String adminCode);
    Boolean existsByEmail(String email);
}
