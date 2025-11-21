package com.example.policlinic.repository;

import com.example.policlinic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByActiveTrue();

    @Query("SELECT u FROM User u WHERE u.role IN ('DOCTOR', 'NURSE') AND u.active = true")
    List<User> findActiveMedicalStaff();
}