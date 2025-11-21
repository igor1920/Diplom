package com.example.policlinic.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diagnosis_rule")
public class DiagnosisRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Column(nullable = false)
    private String diagnosis;

    @Column(name = "symptoms_required", nullable = false)
    private Integer symptomsRequired;

    @ManyToMany
    @JoinTable(
            name = "rule_symptom",
            joinColumns = @JoinColumn(name = "rule_id"),
            inverseJoinColumns = @JoinColumn(name = "symptom_id")
    )
    private List<MedicalSymptom> symptoms = new ArrayList<>();

    // Конструкторы, геттеры, сеттеры
    public DiagnosisRule() {}

    public DiagnosisRule(String ruleName, String diagnosis, Integer symptomsRequired) {
        this.ruleName = ruleName;
        this.diagnosis = diagnosis;
        this.symptomsRequired = symptomsRequired;
    }

    public Long getId() { return id; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public Integer getSymptomsRequired() { return symptomsRequired; }
    public void setSymptomsRequired(Integer symptomsRequired) { this.symptomsRequired = symptomsRequired; }
    public List<MedicalSymptom> getSymptoms() { return symptoms; }
    public void setSymptoms(List<MedicalSymptom> symptoms) { this.symptoms = symptoms; }
}