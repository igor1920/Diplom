package com.example.policlinic.controller;

import com.example.policlinic.model.*;
import com.example.policlinic.repository.*;
import com.example.policlinic.service.ClinicService;
import com.example.policlinic.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ClinicController {

    private final PatientRepository patientRepository;
    private final SymptomRepository symptomRepository;
    private final ClinicService clinicService;
    private final MedicalSymptomRepository medicalSymptomRepository;
    private final TreatmentRepository treatmentRepository;
    private final MedicalTestRepository medicalTestRepository;
    private final DiagnosisRuleRepository diagnosisRuleRepository;
    private final VisitRepository visitRepository;
    private final UserRepository userRepository;

    @Autowired
    private SessionService sessionService;

    public ClinicController(PatientRepository patientRepository,
                            SymptomRepository symptomRepository,
                            ClinicService clinicService,
                            MedicalSymptomRepository medicalSymptomRepository,
                            TreatmentRepository treatmentRepository,
                            MedicalTestRepository medicalTestRepository,
                            DiagnosisRuleRepository diagnosisRuleRepository,
                            VisitRepository visitRepository,
                            UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.symptomRepository = symptomRepository;
        this.clinicService = clinicService;
        this.medicalSymptomRepository = medicalSymptomRepository;
        this.treatmentRepository = treatmentRepository;
        this.medicalTestRepository = medicalTestRepository;
        this.diagnosisRuleRepository = diagnosisRuleRepository;
        this.visitRepository = visitRepository;
        this.userRepository = userRepository;
    }


    private User getCurrentUser() {
        User currentUser = sessionService.getCurrentUser();
        if (currentUser != null) {
            System.out.println("🔐 Текущий пользователь из сессии: " +
                    currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        } else {
            System.out.println("⚠️ Пользователь не аутентифицирован");
        }
        return currentUser;
    }

    // ------------------- Пациенты -------------------
    @GetMapping("/patients")
    public ResponseEntity<?> getPatients() {
        try {
            User currentUser = getCurrentUser();
            System.out.println("🎯 Запрос пациентов от: " +
                    (currentUser != null ? currentUser.getFullName() + " (" + currentUser.getRole() + ")" : "null"));

            List<Patient> patients;

            if (currentUser != null && "DOCTOR".equals(currentUser.getRole())) {
                // Врач видит только своих пациентов
                patients = patientRepository.findByDoctorId(currentUser.getId());
                System.out.println("✅ Врач видит " + patients.size() + " своих пациентов");

            } else if (currentUser != null && "NURSE".equals(currentUser.getRole())) {
                // Медсестра видит всех пациентов поликлиники (только чтение)
                patients = patientRepository.findAll();
                System.out.println("✅ Медсестра видит всех " + patients.size() + " пациентов");

            } else if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                // Администратор видит всех пациентов
                patients = patientRepository.findAll();
                System.out.println("✅ Админ видит всех " + patients.size() + " пациентов");

            } else {
                patients = List.of();
                System.out.println("⚠️ Нет доступа к пациентам");
            }

            // Используем DTO для избежания проблем с lazy loading
            List<Map<String, Object>> patientDtos = patients.stream().map(patient -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", patient.getId());
                dto.put("firstName", patient.getFirstName());
                dto.put("lastName", patient.getLastName());
                dto.put("age", patient.getAge());
                dto.put("address", patient.getAddress());
                dto.put("phone", patient.getPhone());

                // Осторожно обрабатываем врача
                if (patient.getDoctor() != null) {
                    Map<String, Object> doctorInfo = new HashMap<>();
                    doctorInfo.put("id", patient.getDoctor().getId());
                    doctorInfo.put("fullName", patient.getDoctor().getFullName());
                    doctorInfo.put("specialization", patient.getDoctor().getSpecialization());
                    dto.put("doctor", doctorInfo);
                } else {
                    dto.put("doctor", null);
                }

                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(patientDtos);

        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки пациентов: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка загрузки пациентов"));
        }
    }
    @PostMapping("/patients")
    public ResponseEntity<?> addPatient(@RequestBody Patient patient) {
        try {
            User currentDoctor = getCurrentUser();
            System.out.println("🎯 Текущий врач: " + (currentDoctor != null ? currentDoctor.getFullName() : "null"));

            if (currentDoctor != null && "DOCTOR".equals(currentDoctor.getRole())) {
                patient.setDoctor(currentDoctor);
                System.out.println("✅ Пациент привязан к врачу: " + currentDoctor.getFullName());
            } else {
                System.out.println("⚠️ Врач не найден или роль не DOCTOR");
            }

            Patient savedPatient = clinicService.addPatient(patient);
            System.out.println("✅ Пациент сохранен: " + savedPatient.getFullName() +
                    ", Врач: " + (savedPatient.getDoctor() != null ? savedPatient.getDoctor().getFullName() : "null"));

            return ResponseEntity.ok(savedPatient);
        } catch (Exception e) {
            System.err.println("❌ Ошибка создания пациента: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/patients/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @RequestBody Patient patientDetails) {
        try {
            Patient updated = clinicService.updatePatient(id, patientDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/patients/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        if (!patientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        patientRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------- Симптомы -------------------
    @GetMapping("/patients/{id}/symptoms")
    public ResponseEntity<List<Symptom>> getSymptoms(@PathVariable Long id) {
        if (!patientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<Symptom> symptoms = symptomRepository.findByPatientId(id);
        return ResponseEntity.ok(symptoms);
    }

    @PostMapping("/patients/{id}/symptoms")
    public ResponseEntity<Symptom> addSymptom(@PathVariable Long id, @RequestBody Symptom symptom) {
        try {
            Symptom saved = clinicService.addSymptom(id, symptom.getDescription());
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ------------------- Лечение и анализы -------------------
    @GetMapping("/patients/{id}/treatments")
    public ResponseEntity<List<String>> getTreatments(@PathVariable Long id) {
        if (!patientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(clinicService.getTreatmentsForPatient(id));
    }

    @GetMapping("/patients/{id}/tests")
    public ResponseEntity<List<String>> getTests(@PathVariable Long id) {
        if (!patientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(clinicService.getTestsForPatient(id));
    }

    // ------------------- Диагноз и отчёт -------------------
    @GetMapping("/patients/{id}/diagnosis")
    public ResponseEntity<String> getDiagnosis(@PathVariable Long id) {
        if (!patientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        String diagnosis = clinicService.getDiagnosisForPatient(id);
        return ResponseEntity.ok(diagnosis);
    }

    @GetMapping("/patients/{id}/report")
    public ResponseEntity<String> getPatientReport(@PathVariable Long id) {
        if (!patientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        String report = clinicService.getPatientReport(id);
        return ResponseEntity.ok(report);
    }

    // ------------------- Медицинские справочники -------------------
    @GetMapping("/medical-symptoms")
    public List<MedicalSymptom> getAllMedicalSymptoms() {
        return medicalSymptomRepository.findAll();
    }

    @GetMapping("/medical-symptoms/{id}/treatments")
    public List<Treatment> getTreatmentsForSymptom(@PathVariable Long id) {
        return treatmentRepository.findBySymptomId(id);
    }

    @GetMapping("/medical-symptoms/{id}/tests")
    public List<MedicalTest> getTestsForSymptom(@PathVariable Long id) {
        return medicalTestRepository.findBySymptomId(id);
    }

    @GetMapping("/diagnosis-rules")
    public List<DiagnosisRule> getAllDiagnosisRules() {
        return diagnosisRuleRepository.findAll();
    }

    // Автодополнение симптомов
    @GetMapping("/medical-symptoms/search")
    public List<String> searchSymptoms(@RequestParam String query) {
        if (query.length() < 2) return List.of();

        return medicalSymptomRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .map(MedicalSymptom::getName)
                .limit(5)
                .collect(Collectors.toList());
    }

    // ------------------- Визиты -------------------
    @GetMapping("/patients/{id}/visits")
    public ResponseEntity<List<Map<String, Object>>> getPatientVisits(@PathVariable Long id) {
        if (!patientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        try {
            List<Visit> visits = visitRepository.findByPatientIdOrderByVisitDateDesc(id);

            List<Map<String, Object>> visitDtos = visits.stream().map(visit -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", visit.getId());
                dto.put("visitDate", visit.getVisitDate());
                dto.put("visitType", visit.getVisitType());
                dto.put("status", visit.getStatus());
                dto.put("diagnosis", visit.getDiagnosis());
                dto.put("sickLeaveIssued", visit.getSickLeaveIssued());
                dto.put("sickLeaveStart", visit.getSickLeaveStart());
                dto.put("sickLeaveEnd", visit.getSickLeaveEnd());
                dto.put("sickLeaveClosedDate", visit.getSickLeaveClosedDate());
                dto.put("notes", visit.getNotes());

                if (visit.getDoctor() != null) {
                    Map<String, Object> doctorInfo = new HashMap<>();
                    doctorInfo.put("id", visit.getDoctor().getId());
                    doctorInfo.put("fullName", visit.getDoctor().getFullName());
                    doctorInfo.put("specialization", visit.getDoctor().getSpecialization());
                    dto.put("doctor", doctorInfo);
                }

                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(visitDtos);

        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки визитов: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/patients/{id}/visits")
    public ResponseEntity<Map<String, Object>> createVisit(@PathVariable Long id, @RequestBody Visit visit) {
        try {
            System.out.println("=== ДЕБАГ СОЗДАНИЯ ВИЗИТА ===");
            System.out.println("Пациент ID: " + id);

            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Пациент не найден"));
            System.out.println("✅ Пациент найден: " + patient.getFirstName() + " " + patient.getLastName());

            User currentDoctor = getCurrentUser();
            System.out.println("Врач: " + (currentDoctor != null ? currentDoctor.getFullName() : "null"));

            visit.setPatient(patient);
            visit.setDoctor(currentDoctor);
            visit.setVisitDate(LocalDateTime.now());

            if (visit.getStatus() == null) visit.setStatus("в процессе");
            if (visit.getVisitType() == null) visit.setVisitType("первичный");
            if (visit.getSickLeaveIssued() == null) visit.setSickLeaveIssued(false);

            Visit saved = visitRepository.save(visit);
            System.out.println("✅ Визит создан успешно: " + saved.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("message", "Визит успешно создан");
            response.put("visitDate", saved.getVisitDate());
            response.put("visitType", saved.getVisitType());
            response.put("status", saved.getStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ ОШИБКА СОЗДАНИЯ ВИЗИТА:");
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/visits/{id}")
    public ResponseEntity<Map<String, Object>> updateVisit(@PathVariable Long id, @RequestBody Visit visitDetails) {
        try {
            Visit updated = visitRepository.findById(id)
                    .map(visit -> {
                        visit.setDiagnosis(visitDetails.getDiagnosis());
                        visit.setStatus(visitDetails.getStatus());
                        visit.setSickLeaveIssued(visitDetails.getSickLeaveIssued());
                        visit.setSickLeaveStart(visitDetails.getSickLeaveStart());
                        visit.setSickLeaveEnd(visitDetails.getSickLeaveEnd());
                        visit.setNotes(visitDetails.getNotes());
                        return visitRepository.save(visit);
                    })
                    .orElseThrow(() -> new RuntimeException("Визит не найден"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", updated.getId());
            response.put("message", "Визит успешно обновлен");
            response.put("status", updated.getStatus());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/visits/{id}/close-sick-leave")
    public ResponseEntity<Map<String, Object>> closeSickLeave(@PathVariable Long id) {
        try {
            Visit updated = visitRepository.findById(id)
                    .map(visit -> {
                        visit.setSickLeaveIssued(false);
                        visit.setSickLeaveClosedDate(LocalDateTime.now());
                        visit.setStatus("завершен");
                        visit.setSickLeaveStart(null);
                        visit.setSickLeaveEnd(null);
                        return visitRepository.save(visit);
                    })
                    .orElseThrow(() -> new RuntimeException("Визит не найден"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", updated.getId());
            response.put("message", "Больничный закрыт, визит завершен");
            response.put("status", updated.getStatus());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}