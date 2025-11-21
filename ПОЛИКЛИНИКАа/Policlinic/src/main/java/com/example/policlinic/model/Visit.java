package com.example.policlinic.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visit")
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonBackReference("patient-visits")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;


    @Column(nullable = false)
    private LocalDateTime visitDate;
    @Column(name = "sick_leave_closed_date")
    private LocalDateTime sickLeaveClosedDate;
    private String visitType; // первичный, повторный
    private String status; // завершен, в процессе
    private String diagnosis;
    private Boolean sickLeaveIssued = false;
    private LocalDateTime sickLeaveStart;
    private LocalDateTime sickLeaveEnd;
    private String notes;

    // Конструкторы
    public Visit() {}

    public Visit(Patient patient, LocalDateTime visitDate, String visitType) {
        this.patient = patient;
        this.visitDate = visitDate;
        this.visitType = visitType;
        this.status = "в процессе";
    }

    // Геттеры и сеттеры
    public User getDoctor() { return doctor; }
    public void setDoctor(User doctor) { this.doctor = doctor; }
    public Long getId() { return id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public LocalDateTime getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDateTime visitDate) { this.visitDate = visitDate; }
    public String getVisitType() { return visitType; }
    public void setVisitType(String visitType) { this.visitType = visitType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public Boolean getSickLeaveIssued() { return sickLeaveIssued; }
    public void setSickLeaveIssued(Boolean sickLeaveIssued) { this.sickLeaveIssued = sickLeaveIssued; }
    public LocalDateTime getSickLeaveStart() { return sickLeaveStart; }
    public void setSickLeaveStart(LocalDateTime sickLeaveStart) { this.sickLeaveStart = sickLeaveStart; }
    public LocalDateTime getSickLeaveEnd() { return sickLeaveEnd; }
    public void setSickLeaveEnd(LocalDateTime sickLeaveEnd) { this.sickLeaveEnd = sickLeaveEnd; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getSickLeaveClosedDate() { return sickLeaveClosedDate; }
    public void setSickLeaveClosedDate(LocalDateTime sickLeaveClosedDate) { this.sickLeaveClosedDate = sickLeaveClosedDate; }
}