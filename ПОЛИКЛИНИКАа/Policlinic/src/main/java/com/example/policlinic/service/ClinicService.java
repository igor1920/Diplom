package com.example.policlinic.service;

import com.example.policlinic.model.Patient;
import com.example.policlinic.model.Symptom;
import com.example.policlinic.repository.PatientRepository;
import com.example.policlinic.repository.SymptomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ClinicService — расширенная экспертная логика для дипломного проекта.
 * Назначения и анализы носят рекомендательный характер.
 */
@Service
public class ClinicService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private SymptomRepository symptomRepository;

    // ---- Расширенная база назначений (с примерной дозировкой/подсказкой) ----
    private static final Map<String, List<String>> TREATMENT_MAP = new LinkedHashMap<>();
    private static final Map<String, List<String>> TEST_MAP = new LinkedHashMap<>();
    private static final List<DiagnosisRule> DIAGNOSIS_RULES = new ArrayList<>();

    static {
        // ТREATMENTS (ключ — ключевое слово симптома)
        TREATMENT_MAP.put("кашель", List.of(
                "Амброксол — 30 мг 2 раза в сутки (детям и взрослым по рекомендации)",
                "Бромгексин — 8–16 мг 3 раза в сутки",
                "Муколитики: Мукалтин по инструкции"
        ));
        TREATMENT_MAP.put("температура", List.of(
                "Парацетамол — 500 мг при повышении температуры (макс 3 г/сут)",
                "Ибупрофен — 200–400 мг при боли/жаре"
        ));
        TREATMENT_MAP.put("головная боль", List.of(
                "Парацетамол 500 мг при боли",
                "Ибупрофен 200–400 мг при необходимости"
        ));
        TREATMENT_MAP.put("боль в горле", List.of(
                "Полоскания (раствор фурацилина/соль) и Тантум Верде",
                "Спреи: Мирамистин / Хлоргексидин"
        ));
        TREATMENT_MAP.put("насморк", List.of(
                "Сосудосуживающие капли (Називин) — кратковременно",
                "Физиологический раствор (Аква Марис) для промываний"
        ));
        TREATMENT_MAP.put("боль в животе", List.of(
                "Но-шпа (дротаверин) — 40 мг при спазмах",
                "Смекта для симптоматического лечения диареи"
        ));
        TREATMENT_MAP.put("диарея", List.of(
                "Регидрон — регидратация",
                "Смекта — адсорбент при острой диарее"
        ));
        TREATMENT_MAP.put("тошнота", List.of(
                "Метоклопрамид (Церукал) по назначению врача",
                "Диета и обильное питьё"
        ));
        TREATMENT_MAP.put("сыпь", List.of(
                "Антигистаминные: Цетиризин 10 мг 1 раз в сутки",
                "Местные средства: Фенистил-гель"
        ));
        TREATMENT_MAP.put("усталость", List.of(
                "Витамины: Компливит/Мультивитамины, Магний B6",
                "Режим сна и восстановление"
        ));
        TREATMENT_MAP.put("простуда", List.of(
                "Профильтные средства: парацетамол по симптомам, местные капли",
                "Покой, обильное питьё"
        ));
        TREATMENT_MAP.put("боль в теле", List.of(
                "Ибупрофен или Парацетамол при болях в мышцах",
                "При сильной боли — НПВС (по назначению врача, напр. Мовалис)"
        ));
        TREATMENT_MAP.put("боль в спине", List.of(
                "Местные мази (Диклофенак гель), физиотерапия",
                "При остром болевом синдроме — НПВС по назначению"
        ));

        // ---- TESTS ----
        TEST_MAP.put("кашель", List.of("Рентген грудной клетки", "Общий анализ крови", "ПЦР/мазок при подозрении на вирус"));
        TEST_MAP.put("температура", List.of("Общий анализ крови", "Анализ мочи"));
        TEST_MAP.put("головная боль", List.of("Измерение АД", "МРТ/КТ при тревожных признаках"));
        TEST_MAP.put("боль в горле", List.of("Мазок из зева", "Общий анализ крови"));
        TEST_MAP.put("насморк", List.of("Анализ крови на аллергены при подозрении"));
        TEST_MAP.put("боль в животе", List.of("УЗИ органов брюшной полости", "Биохимия крови"));
        TEST_MAP.put("диарея", List.of("Общий анализ кала", "Общий анализ крови", "Регидратация"));
        TEST_MAP.put("сыпь", List.of("Анализы на аллергены", "Кожные пробы"));
        TEST_MAP.put("усталость", List.of("Общий анализ крови", "Тест на витамин D", "Глюкоза"));
        TEST_MAP.put("боль в теле", List.of("Общий анализ крови", "С-реактивный белок (СРБ)", "Ревмопробы при подозрении"));

        // ---- DIAGNOSIS RULES (набор ключевых симптомов -> диагноз) ----
        // Чем более специфичные правила — тем выше приоритет (добавляй сверху)
        DIAGNOSIS_RULES.add(new DiagnosisRule(Set.of("кашель", "температура", "насморк"), "ОРВИ / Острый респираторный вирус"));
        DIAGNOSIS_RULES.add(new DiagnosisRule(Set.of("кашель", "температура", "боль в теле"), "Грипп — рассмотреть антигриппозную терапию"));
        DIAGNOSIS_RULES.add(new DiagnosisRule(Set.of("кашель", "боль в горле"), "Трахеобронхит / фарингит"));
        DIAGNOSIS_RULES.add(new DiagnosisRule(Set.of("боль в животе", "диарея", "тошнота"), "Острое кишечное отравление / гастроэнтерит"));
        DIAGNOSIS_RULES.add(new DiagnosisRule(Set.of("сыпь", "температура"), "Аллергическая реакция / инфекционная сыпь — требуется обследование"));
        DIAGNOSIS_RULES.add(new DiagnosisRule(Set.of("усталость", "боль в теле"), "Переутомление / поствирусный синдром"));
        DIAGNOSIS_RULES.add(new DiagnosisRule(Set.of("боль в спине", "боль в теле"), "Мышечный/скелетный синдром — физиотерапия"));
        // Общие шаблоны
        DIAGNOSIS_RULES.add(new DiagnosisRule(Set.of("кашель"), "Кашель — уточнить характер (сухой/влажный)"));
        DIAGNOSIS_RULES.add(new DiagnosisRule(Set.of("головная боль"), "Головная боль — исключить гипертонию или мигрень"));
    }

    // ---- CRUD для пациентов/симптомов ----
    public Patient addPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Long id, Patient updatedPatient) {
        return patientRepository.findById(id)
                .map(p -> {
                    p.setFirstName(updatedPatient.getFirstName());
                    p.setLastName(updatedPatient.getLastName());
                    p.setAge(updatedPatient.getAge());
                    p.setAddress(updatedPatient.getAddress());
                    p.setPhone(updatedPatient.getPhone());
                    return patientRepository.save(p);
                })
                .orElseThrow(() -> new RuntimeException("Пациент не найден"));
    }

    public Symptom addSymptom(Long patientId, String description) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Пациент не найден"));

        String treatment = assignTreatment(description);
        Symptom symptom = new Symptom();
        symptom.setDescription(description);
        symptom.setTreatment(treatment);
        symptom.setPatient(patient);

        return symptomRepository.save(symptom);
    }

    // ---- Получение назначений / анализов по пациенту ----
    public List<String> getTreatmentsForPatient(Long patientId) {
        List<Symptom> symptoms = symptomRepository.findByPatientId(patientId);
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (Symptom s : symptoms) {
            String desc = s.getDescription().toLowerCase();
            for (Map.Entry<String, List<String>> entry : TREATMENT_MAP.entrySet()) {
                if (desc.contains(entry.getKey())) {
                    result.addAll(entry.getValue());
                }
            }
            // Если симптом уже имеет treatment в записи — берём и его
            if (s.getTreatment() != null && !s.getTreatment().isBlank()) result.add(s.getTreatment());
        }
        if (result.isEmpty()) result.add("Лечение не назначено — требуется консультация врача");
        return new ArrayList<>(result);
    }

    public List<String> getTestsForPatient(Long patientId) {
        List<Symptom> symptoms = symptomRepository.findByPatientId(patientId);
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (Symptom s : symptoms) {
            String desc = s.getDescription().toLowerCase();
            for (Map.Entry<String, List<String>> entry : TEST_MAP.entrySet()) {
                if (desc.contains(entry.getKey())) {
                    result.addAll(entry.getValue());
                }
            }
        }
        if (result.isEmpty()) result.add("Анализы не назначены");
        return new ArrayList<>(result);
    }

    // ---- Диагностика: на основе набора симптомов ----
    public String getDiagnosisForPatient(Long patientId) {
        List<Symptom> symptoms = symptomRepository.findByPatientId(patientId);
        Set<String> present = new HashSet<>();
        for (Symptom s : symptoms) {
            // учитываем ключевые слова, а не полную строку
            String desc = s.getDescription().toLowerCase();
            for (String key : unionKeys()) {
                if (desc.contains(key)) present.add(key);
            }
        }

        // Проверяем правила в порядке приоритета
        for (DiagnosisRule rule : DIAGNOSIS_RULES) {
            if (present.containsAll(rule.keys)) {
                return rule.diagnosis;
            }
        }

        if (present.isEmpty()) return "Диагноз не определён — симптомов мало";
        return "Неоднозначно — требуется обследование (список симптомов: " + String.join(", ", present) + ")";
    }

    // ---- Полный текстовый отчёт по пациенту (симптомы → лечение → анализы → диагноз → рекомендации) ----
    public String getPatientReport(Long patientId) {
        Optional<Patient> op = patientRepository.findById(patientId);
        if (op.isEmpty()) return "Пациент не найден";

        Patient p = op.get();
        List<Symptom> symptoms = symptomRepository.findByPatientId(patientId);
        StringBuilder sb = new StringBuilder();

        sb.append("Отчёт пациента: ").append(p.getFirstName()).append(" ").append(p.getLastName())
                .append(" (ID: ").append(p.getId()).append(")\n");
        sb.append("Дата: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");

        sb.append("Симптомы:\n");
        if (symptoms.isEmpty()) sb.append("  — Симптомы отсутствуют\n");
        else for (Symptom s : symptoms) sb.append("  - ").append(s.getDescription())
                .append(s.getTreatment() != null ? " (записанное лечение: " + s.getTreatment() + ")" : "")
                .append("\n");

        sb.append("\nРекомендованное лечение:\n");
        for (String t : getTreatmentsForPatient(patientId)) sb.append("  - ").append(t).append("\n");

        sb.append("\nРекомендованные анализы:\n");
        for (String t : getTestsForPatient(patientId)) sb.append("  - ").append(t).append("\n");

        sb.append("\nПредполагаемый диагноз:\n");
        sb.append("  ").append(getDiagnosisForPatient(patientId)).append("\n\n");

        sb.append("Рекомендации:\n");
        sb.append("  - При ухудшении состояния — немедленно обратиться к врачу.\n");
        sb.append("  - Не применять препараты без учета противопоказаний и возраста.\n");
        sb.append("  - Следовать режиму покоя и диете при симптомах ЖКТ.\n");

        return sb.toString();
    }

    // ---- Вспомогательные методы ----
    private String assignTreatment(String description) {
        String lower = description.toLowerCase();
        for (Map.Entry<String, List<String>> entry : TREATMENT_MAP.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return String.join(", ", entry.getValue());
            }
        }
        return "Консультация врача";
    }

    private Set<String> unionKeys() {
        // ключи, по которым мы ищем присутствие симптомов
        return new HashSet<>(TREATMENT_MAP.keySet());
    }

    // ---- Внутренний класс для правил диагноза ----
    private static class DiagnosisRule {
        final Set<String> keys;
        final String diagnosis;
        DiagnosisRule(Set<String> keys, String diagnosis) {
            this.keys = keys;
            this.diagnosis = diagnosis;
        }
    }
}
