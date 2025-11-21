package com.example.policlinic.repository;

import com.example.policlinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Найти пациентов по ID врача
    List<Patient> findByDoctorId(Long doctorId);

    // Найти пациентов по имени и фамилии
    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    // Найти пациентов по врачу и имени
    @Query("SELECT p FROM Patient p WHERE p.doctor.id = :doctorId AND " +
            "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Patient> findByDoctorAndName(@Param("doctorId") Long doctorId, @Param("query") String query);

    // Проверить существование пациента по телефону
    boolean existsByPhone(String phone);
}