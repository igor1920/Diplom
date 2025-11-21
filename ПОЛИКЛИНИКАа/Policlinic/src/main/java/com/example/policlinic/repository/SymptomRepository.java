package com.example.policlinic.repository;


import com.example.policlinic.model.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SymptomRepository extends JpaRepository<Symptom, Long> {
    List<Symptom> findByPatientId(Long patientId);
}