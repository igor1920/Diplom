package com.example.policlinic.service;

import com.example.policlinic.model.User;
import com.example.policlinic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionService sessionService;

    // ★★★ СОБСТВЕННАЯ РЕАЛИЗАЦИЯ ХЕШИРОВАНИЯ ★★★
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка хеширования пароля", e);
        }
    }

    private boolean verifyPassword(String password, String hashedPassword) {
        return hashPassword(password).equals(hashedPassword);
    }

    @PostConstruct
    public void init() {
        createDefaultUsers();
    }

    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User register(User user) {
        // Проверка уникальности username
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Пользователь с логином '" + user.getUsername() + "' уже существует");
        }

        // Проверка уникальности email (если указан)
        if (user.getEmail() != null && !user.getEmail().isEmpty() &&
                userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Пользователь с email '" + user.getEmail() + "' уже существует");
        }

        // Валидация роли
        if (!isValidRole(user.getRole())) {
            throw new RuntimeException("Недопустимая роль: " + user.getRole());
        }

        // ★★★ ХЕШИРУЕМ ПАРОЛЬ ПЕРЕД СОХРАНЕНИЕМ ★★★
        user.setPassword(hashPassword(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> login(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && user.get().getActive()) {
            User foundUser = user.get();

            // ★★★ ПРОВЕРЯЕМ ПАРОЛЬ С ХЕШИРОВАНИЕМ ★★★
            if (verifyPassword(password, foundUser.getPassword())) {
                foundUser.setLastLogin(java.time.LocalDateTime.now());
                userRepository.save(foundUser);

                // Сохраняем пользователя в сессии
                sessionService.setCurrentUser(foundUser);

                return Optional.of(foundUser);
            } else {
                System.out.println("❌ Неверный пароль для пользователя: " + username);
            }
        } else {
            System.out.println("❌ Пользователь не найден или неактивен: " + username);
        }
        return Optional.empty();
    }

    public void logout() {
        sessionService.logout();
    }

    public boolean isValidRole(String role) {
        return role != null && (role.equals("ADMIN") || role.equals("DOCTOR") || role.equals("NURSE"));
    }

    public void createDefaultUsers() {
        if (userRepository.count() == 0) {
            System.out.println("🔄 Создание тестовых пользователей с безопасными паролями...");

            // ★★★ СОЗДАЕМ УНИКАЛЬНЫЕ ПАРОЛИ ДЛЯ КАЖДОГО ПОЛЬЗОВАТЕЛЯ ★★★

            // Администратор
            User admin = new User("admin", "AdminSecure123!", "Администратор Системы", "ADMIN");
            admin.setEmail("admin@polyclinic.ru");
            admin.setPhone("+7 (999) 123-45-67");
            admin.setPassword(hashPassword(admin.getPassword())); // Хешируем
            userRepository.save(admin);

            // Врач-терапевт 1
            User doctor1 = new User("doctor.ivanov", "IvanovMed2024!", "Иванов Иван Иванович", "DOCTOR");
            doctor1.setSpecialization("Терапевт");
            doctor1.setEmail("i.ivanov@polyclinic.ru");
            doctor1.setPhone("+7 (999) 123-45-68");
            doctor1.setPassword(hashPassword(doctor1.getPassword())); // Хешируем
            userRepository.save(doctor1);

            // Врач-хирург
            User doctor2 = new User("doctor.petrov", "PetrovSurg2024!", "Петров Петр Петрович", "DOCTOR");
            doctor2.setSpecialization("Хирург");
            doctor2.setEmail("p.petrov@polyclinic.ru");
            doctor2.setPhone("+7 (999) 123-45-69");
            doctor2.setPassword(hashPassword(doctor2.getPassword())); // Хешируем
            userRepository.save(doctor2);

            // Врач-кардиолог
            User doctor3 = new User("doctor.sidorov", "SidorovCardio2024!", "Сидоров Алексей Владимирович", "DOCTOR");
            doctor3.setSpecialization("Кардиолог");
            doctor3.setEmail("a.sidorov@polyclinic.ru");
            doctor3.setPhone("+7 (999) 123-45-71");
            doctor3.setPassword(hashPassword(doctor3.getPassword())); // Хешируем
            userRepository.save(doctor3);

            // Медсестра
            User nurse1 = new User("nurse.sidorova", "NurseSecure2024!", "Сидорова Мария Ивановна", "NURSE");
            nurse1.setSpecialization("Старшая медсестра");
            nurse1.setEmail("m.sidorova@polyclinic.ru");
            nurse1.setPhone("+7 (999) 123-45-70");
            nurse1.setPassword(hashPassword(nurse1.getPassword())); // Хешируем
            userRepository.save(nurse1);

            // Медсестра процедурного кабинета
            User nurse2 = new User("nurse.ivanova", "IvanovaNurse2024!", "Иванова Ольга Сергеевна", "NURSE");
            nurse2.setSpecialization("Процедурная медсестра");
            nurse2.setEmail("o.ivanova@polyclinic.ru");
            nurse2.setPhone("+7 (999) 123-45-72");
            nurse2.setPassword(hashPassword(nurse2.getPassword())); // Хешируем
            userRepository.save(nurse2);

            System.out.println("✅ Тестовые пользователи созданы с безопасными паролями!");
        } else {
            System.out.println("ℹ️ Пользователи уже существуют в базе");
        }
    }

    // ★★★ МЕТОД ДЛЯ СМЕНЫ ПАРОЛЯ ★★★
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Проверяем старый пароль
            if (verifyPassword(oldPassword, user.getPassword())) {
                // Хешируем и сохраняем новый пароль
                user.setPassword(hashPassword(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    // ★★★ МЕТОД ДЛЯ ПРОВЕРКИ СЛОЖНОСТИ ПАРОЛЯ ★★★
    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        // Проверяем наличие цифр, букв в разных регистрах и специальных символов
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return hasDigit && hasLower && hasUpper && hasSpecial;
    }
}