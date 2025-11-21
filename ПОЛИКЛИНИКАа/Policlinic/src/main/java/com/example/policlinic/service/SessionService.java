package com.example.policlinic.service;

import com.example.policlinic.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    @Autowired
    private HttpSession httpSession;

    private static final String CURRENT_USER_KEY = "CURRENT_USER";

    public void setCurrentUser(User user) {
        httpSession.setAttribute(CURRENT_USER_KEY, user);
        httpSession.setMaxInactiveInterval(60 * 60); // 1 час
        System.out.println("🔐 Пользователь сохранен в сессии: " + user.getFullName());
    }

    public User getCurrentUser() {
        User user = (User) httpSession.getAttribute(CURRENT_USER_KEY);
        if (user != null) {
            System.out.println("🔐 Пользователь получен из сессии: " + user.getFullName());
        } else {
            System.out.println("⚠️ Пользователь не найден в сессии");
        }
        return user;
    }

    public void logout() {
        User user = getCurrentUser();
        httpSession.removeAttribute(CURRENT_USER_KEY);
        httpSession.invalidate();
        System.out.println("🔐 Пользователь вышел из системы: " + (user != null ? user.getFullName() : "unknown"));
    }

    public boolean isAuthenticated() {
        return getCurrentUser() != null;
    }
}