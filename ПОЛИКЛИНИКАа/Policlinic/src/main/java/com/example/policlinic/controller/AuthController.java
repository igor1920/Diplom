package com.example.policlinic.controller;

import com.example.policlinic.model.User;
import com.example.policlinic.service.AuthService;
import com.example.policlinic.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private SessionService sessionService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registered = authService.register(user);
            return ResponseEntity.ok(Map.of(
                    "message", "Регистрация успешна",
                    "user", createUserResponse(registered)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        try {
            User currentUser = sessionService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Пользователь не аутентифицирован"));
            }

            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Старый и новый пароль обязательны"));
            }

            // Проверяем сложность нового пароля
            if (!authService.isPasswordStrong(newPassword)) {
                return ResponseEntity.badRequest().body(Map.of("error",
                        "Новый пароль должен содержать минимум 8 символов, включая цифры, буквы в верхнем и нижнем регистре, и специальные символы"));
            }

            boolean success = authService.changePassword(currentUser.getId(), oldPassword, newPassword);

            if (success) {
                return ResponseEntity.ok(Map.of("message", "Пароль успешно изменен"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Неверный старый пароль"));
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка смены пароля: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка смены пароля"));
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> user = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (user.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "message", "Вход выполнен успешно",
                    "user", createUserResponse(user.get())
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Неверный логин или пароль"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        authService.logout();
        return ResponseEntity.ok(Map.of("message", "Выход выполнен успешно"));
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser() {
        User currentUser = sessionService.getCurrentUser();
        if (currentUser != null) {
            return ResponseEntity.ok(createUserResponse(currentUser));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Пользователь не аутентифицирован"));
        }
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@PathVariable String username) {
        boolean exists = authService.checkUsernameExists(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@PathVariable String email) {
        boolean exists = authService.checkEmailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId() != null ? user.getId() : 0);
        userMap.put("username", user.getUsername() != null ? user.getUsername() : "");
        userMap.put("fullName", user.getFullName() != null ? user.getFullName() : "");
        userMap.put("role", user.getRole() != null ? user.getRole() : "");
        userMap.put("specialization", user.getSpecialization() != null ? user.getSpecialization() : "");
        userMap.put("phone", user.getPhone() != null ? user.getPhone() : "");
        userMap.put("email", user.getEmail() != null ? user.getEmail() : "");
        userMap.put("lastLogin", user.getLastLogin() != null ? user.getLastLogin() : "");
        return userMap;
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}