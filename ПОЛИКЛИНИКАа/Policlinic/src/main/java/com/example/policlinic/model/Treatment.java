package com.example.policlinic.model;

import jakarta.persistence.*;

@Entity
@Table(name = "treatment")
public class Treatment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symptom_id")
    private MedicalSymptom symptom;

    @Column(nullable = false)
    private String medication;

    private String dosage;
    private String instructions;

    // Конструкторы
    public Treatment() {}

    public Treatment(MedicalSymptom symptom, String medication, String dosage, String instructions) {
        this.symptom = symptom;
        this.medication = medication;
        this.dosage = dosage;
        this.instructions = instructions;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }

    public MedicalSymptom getSymptom() { return symptom; }
    public void setSymptom(MedicalSymptom symptom) { this.symptom = symptom; }

    public String getMedication() { return medication; }
    public void setMedication(String medication) { this.medication = medication; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}