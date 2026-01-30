package com.sayedhesham.userservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sayedhesham.userservice.dto.UserDTO;
import com.sayedhesham.userservice.dto.UserPatchDTO;
import com.sayedhesham.userservice.model.User;
import com.sayedhesham.userservice.repository.UserRepository;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepo;

    @Autowired
    private AvatarEventService avatarEventService;

    public UserService(UserRepository userRepository) {
        this.userRepo = userRepository;
    }

    public List<UserDTO> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(user -> UserDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build())
                .toList();
    }

    public UserDTO getById(String id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        return UserDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .avatarMediaId(user.getAvatarMediaId())
                .build();
    }

    public UserDTO update(String id, UserPatchDTO user) {
        User existingUser = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        updateBasicFields(existingUser, user);
        updateRole(existingUser, user.getRole());
        handleAvatarUpdate(id, user.getAvatarBase64());

        User updatedUser = userRepo.save(existingUser);
        return UserDTO.builder()
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .role(updatedUser.getRole())
                .avatarMediaId(updatedUser.getAvatarMediaId())
                .build();
    }

    private void updateBasicFields(User existingUser, UserPatchDTO user) {
        if (user.getName() != null && !user.getName().isEmpty()) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null && user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            existingUser.setEmail(user.getEmail());
        }
    }

    private void updateRole(User existingUser, String role) {
        if (role == null) {
            return;
        }
        if (role.equals("user") || role.equals("admin")) {
            existingUser.setRole(role);
        } else {
            throw new RuntimeException("Invalid role");
        }
    }

    private void handleAvatarUpdate(String userId, String avatarBase64) {
        if (avatarBase64 == null || avatarBase64.isEmpty()) {
            return;
        }

        validateAvatarSize(avatarBase64);
        String contentType = extractContentType(avatarBase64);
        avatarEventService.publishAvatarUpdateEvent(userId, avatarBase64, contentType);
    }

    private void validateAvatarSize(String avatarBase64) {
        String base64Data = avatarBase64;

        // Extract base64 data if it contains data URI prefix
        if (base64Data.startsWith("data:image/")) {
            int commaIndex = base64Data.indexOf(',');
            if (commaIndex != -1) {
                base64Data = base64Data.substring(commaIndex + 1);
            }
        }

        // Calculate size in bytes (base64 is ~33% larger than original)
        int imageSizeBytes = (base64Data.length() * 3) / 4;
        int maxFileSizeBytes = 2 * 1024 * 1024; // 2MB

        if (imageSizeBytes > maxFileSizeBytes) {
            throw new RuntimeException("Avatar image size exceeds 2MB limit");
        }
    }

    private String extractContentType(String avatarBase64) {
        if (avatarBase64.startsWith("data:image/")) {
            String[] parts = avatarBase64.split(";");
            if (parts.length > 0) {
                return parts[0].replace("data:", "");
            }
        }
        return "image/jpeg"; // Default content type
    }

    public void delete(String id) {
        User existingUser = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        userRepo.delete(existingUser);
    }
}
