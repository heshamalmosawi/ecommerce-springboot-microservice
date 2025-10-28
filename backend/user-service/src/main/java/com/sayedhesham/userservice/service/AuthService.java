package com.sayedhesham.userservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sayedhesham.userservice.dto.LoginRequest;
import com.sayedhesham.userservice.dto.RegisterRequest;
import com.sayedhesham.userservice.model.User;
import com.sayedhesham.userservice.repository.UserRepository;
import com.sayedhesham.userservice.service.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isEmailTaken(String email) {
        return userRepo.findByEmail(email).isPresent();
    }

    public void registerUser(RegisterRequest req) {
        if (req.getName() == null || req.getName().isEmpty()) {
            throw new RuntimeException("Name is required");
        }
        if (req.getEmail() == null || req.getEmail().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (req.getPassword() == null || req.getPassword().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (req.getRole() == null || req.getRole().isEmpty()) {
            throw new RuntimeException("Role is required");
        }
        if (!req.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$")) {
            throw new RuntimeException("Invalid email format");
        }
        if (req.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        if (req.getRole() == null || (!req.getRole().equals("user") && !req.getRole().equals("admin"))) {
            throw new RuntimeException("Invalid role. Must be 'user' or 'admin'");
        }
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .build();

        this.userRepo.save(user);
    }

    public String loginUser(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return JwtService.generateToken(user);
    }
}
