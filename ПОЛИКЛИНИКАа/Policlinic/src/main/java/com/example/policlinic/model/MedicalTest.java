package com.example.policlinic.model;

import jakarta.persistence.*;

@Entity
@Table(name = "medical_test")
public class MedicalTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symptom_id")
    private MedicalSymptom symptom;

    @Column(name = "test_name", nullable = false)
    private String testName;

    private String description;

    // Конструкторы
    public MedicalTest() {}

    public MedicalTest(MedicalSymptom symptom, String testName, String description) {
        this.symptom = symptom;
        this.testName = testName;
        this.description = description;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }

    public MedicalSymptom getSymptom() { return symptom; }
    public void setSymptom(MedicalSymptom symptom) { this.symptom = symptom; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}