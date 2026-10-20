package com.eswar.uscistracker.service;

import com.eswar.uscistracker.domain.AppUser;
import com.eswar.uscistracker.repository.AppUserRepository;
import com.eswar.uscistracker.web.RegisterForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegisterForm form) {
        String username = form.getUsername().trim();
        String email = normalizeEmail(form.getEmail());

        if (users.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("That username is already in use.");
        }
        if (email != null && users.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("That email is already in use.");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        return users.save(user);
    }

    public AppUser requireByUsername(String username) {
        return users.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new IllegalStateException("User not found."));
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        AppUser user = requireByUsername(username);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New passwords do not match.");
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        users.save(user);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return email.trim().toLowerCase();
    }
}
