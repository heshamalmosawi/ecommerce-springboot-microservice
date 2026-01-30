package com.sayedhesham.userservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sayedhesham.userservice.dto.LoginRequest;
import com.sayedhesham.userservice.dto.LoginResponse;
import com.sayedhesham.userservice.dto.RegisterRequest;
import com.sayedhesham.userservice.model.User;
import com.sayedhesham.userservice.repository.UserRepository;
import com.sayedhesham.userservice.service.security.JwtService;

@Service
public class AuthService {

    private static final long TOKEN_EXPIRATION_MS = 1000L * 60 * 60 * 24 * 3; // 3 days in milliseconds

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AvatarEventService avatarEventService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, AvatarEventService avatarEventService, JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.avatarEventService = avatarEventService;
        this.jwtService = jwtService;
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

        if (req.getRole() == null || (!req.getRole().equals("client") && !req.getRole().equals("seller"))) {
            throw new RuntimeException("Invalid role. Must be 'client' or 'sellers'");
        }

        if (!req.getRole().equals("seller") && (req.getAvatar_b64() != null && !req.getAvatar_b64().isEmpty())) {
            throw new RuntimeException("Only sellers can have an avatar");
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .build();

        User savedUser = this.userRepo.save(user);

        // Publish avatar upload event if seller has avatar
        if (req.getRole().equals("seller") && req.getAvatar_b64() != null && !req.getAvatar_b64().isEmpty()) {
            String contentType = "image/jpeg"; // Default, could be detected from base64
            avatarEventService.publishAvatarUploadEvent(savedUser.getId(), req.getAvatar_b64(), contentType);
        }
    }

    public LoginResponse loginUser(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return LoginResponse.builder()
                .token(jwtService.generateToken(user))
                .expiresAt(System.currentTimeMillis() + TOKEN_EXPIRATION_MS)
                .name(user.getName())
                .build();
    }
}
