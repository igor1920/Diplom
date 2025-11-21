package com.example.policlinic.repository;

import com.example.policlinic.model.MedicalSymptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MedicalSymptomRepository extends JpaRepository<MedicalSymptom, Long> {
    Optional<MedicalSymptom> findByName(String name);
    List<MedicalSymptom> findByNameContainingIgnoreCase(String keyword);

    @Query("SELECT ms FROM MedicalSymptom ms WHERE LOWER(ms.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MedicalSymptom> findByKeyword(@Param("keyword") String keyword);
}