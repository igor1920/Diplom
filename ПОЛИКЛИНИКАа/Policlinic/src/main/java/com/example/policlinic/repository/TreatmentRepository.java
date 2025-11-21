package com.example.policlinic.repository;

import com.example.policlinic.model.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    List<Treatment> findBySymptomId(Long symptomId);

    @Query("SELECT t FROM Treatment t JOIN t.symptom ms WHERE LOWER(ms.name) LIKE LOWER(CONCAT('%', :symptomName, '%'))")
    List<Treatment> findBySymptomNameContaining(@Param("symptomName") String symptomName);
}