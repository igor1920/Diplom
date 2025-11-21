package com.example.policlinic.controller;

import com.example.policlinic.model.User;
import com.example.policlinic.repository.UserRepository;
import com.example.policlinic.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    // ★★★ ДОБАВИТЬ ЭТОТ МЕТОД ★★★
    private User getCurrentUser() {
        // Временная реализация - в реальном приложении используйте Spring Security
        return userRepository.findByUsername("doctor.ivanov")
                .orElseGet(() -> userRepository.findByRole("DOCTOR")
                        .stream()
                        .findFirst()
                        .orElse(null));
    }

    // Получить всех пользователей (только для ADMIN)
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        User currentUser = getCurrentUser();
        if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
            return ResponseEntity.ok(userRepository.findAll());
        }
        return ResponseEntity.status(403).build();
    }

    // Получить пользователя по ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Получить медицинский персонал (врачи и медсестры)
    @GetMapping("/medical-staff")
    public ResponseEntity<List<User>> getMedicalStaff() {
        return ResponseEntity.ok(userRepository.findActiveMedicalStaff());
    }

    // Создать нового пользователя (только для ADMIN)
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return ResponseEntity.status(403).body(Map.of("error", "Доступ запрещен"));
        }

        try {
            User created = authService.register(user);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Обновить пользователя
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return ResponseEntity.status(403).body(Map.of("error", "Доступ запрещен"));
        }

        try {
            User updated = userRepository.findById(id)
                    .map(user -> {
                        user.setFullName(userDetails.getFullName());
                        user.setSpecialization(userDetails.getSpecialization());
                        user.setPhone(userDetails.getPhone());
                        user.setEmail(userDetails.getEmail());
                        user.setRole(userDetails.getRole());
                        user.setActive(userDetails.getActive());
                        return userRepository.save(user);
                    })
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Удалить пользователя (деактивировать)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return ResponseEntity.status(403).body(Map.of("error", "Доступ запрещен"));
        }

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            user.setActive(false);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Пользователь деактивирован"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ★★★ ДОБАВИТЬ ЭТОТ МЕТОД ДЛЯ ПОЛУЧЕНИЯ ПАЦИЕНТОВ ВРАЧА ★★★
    @GetMapping("/my-patients")
    public ResponseEntity<List<Object>> getMyPatients() {
        User currentDoctor = getCurrentUser();
        if (currentDoctor != null && "DOCTOR".equals(currentDoctor.getRole())) {
            // Здесь должна быть логика получения пациентов врача
            // Пока возвращаем пустой список
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.status(403).build();
    }
}