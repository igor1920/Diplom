package com.example.policlinic.repository;

import com.example.policlinic.model.DiagnosisRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DiagnosisRuleRepository extends JpaRepository<DiagnosisRule, Long> {

    @Query("SELECT dr FROM DiagnosisRule dr JOIN dr.symptoms ms " +
            "WHERE ms.name IN :symptomNames " +
            "GROUP BY dr HAVING COUNT(DISTINCT ms) >= dr.symptomsRequired")
    List<DiagnosisRule> findMatchingRules(@Param("symptomNames") List<String> symptomNames);
}