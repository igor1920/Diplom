package com.example.policlinic.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medical_symptom")
public class MedicalSymptom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @OneToMany(mappedBy = "symptom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Treatment> treatments = new ArrayList<>();

    @OneToMany(mappedBy = "symptom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicalTest> tests = new ArrayList<>();

    @ManyToMany(mappedBy = "symptoms", fetch = FetchType.LAZY)
    private List<DiagnosisRule> diagnosisRules = new ArrayList<>();

    // Конструкторы
    public MedicalSymptom() {}

    public MedicalSymptom(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Treatment> getTreatments() { return treatments; }
    public void setTreatments(List<Treatment> treatments) { this.treatments = treatments; }

    public List<MedicalTest> getTests() { return tests; }
    public void setTests(List<MedicalTest> tests) { this.tests = tests; }

    public List<DiagnosisRule> getDiagnosisRules() { return diagnosisRules; }
    public void setDiagnosisRules(List<DiagnosisRule> diagnosisRules) { this.diagnosisRules = diagnosisRules; }
}