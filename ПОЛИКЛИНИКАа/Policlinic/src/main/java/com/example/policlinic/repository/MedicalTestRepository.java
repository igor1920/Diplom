package com.example.policlinic.repository;

import com.example.policlinic.model.MedicalTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MedicalTestRepository extends JpaRepository<MedicalTest, Long> {
    List<MedicalTest> findBySymptomId(Long symptomId);

    @Query("SELECT mt FROM MedicalTest mt JOIN mt.symptom ms WHERE LOWER(ms.name) LIKE LOWER(CONCAT('%', :symptomName, '%'))")
    List<MedicalTest> findBySymptomNameContaining(@Param("symptomName") String symptomName);
}