package com.example.policlinic.repository;

import com.example.policlinic.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    List<Visit> findByPatientIdOrderByVisitDateDesc(Long patientId);
    List<Visit> findByVisitDateBetween(LocalDateTime start, LocalDateTime end);
    List<Visit> findBySickLeaveIssuedTrueAndSickLeaveEndAfter(LocalDateTime date);

    @Query("SELECT v FROM Visit v WHERE v.sickLeaveIssued = false AND v.sickLeaveClosedDate IS NOT NULL AND v.patient.id = :patientId")
    List<Visit> findClosedSickLeavesByPatientId(@Param("patientId") Long patientId);
}
