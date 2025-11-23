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
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .avatarMediaId(user.getAvatarMediaId())
                .build();
    }

    public UserDTO update(String id, UserPatchDTO user) {
        User existingUser = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getName() != null && !user.getName().isEmpty()) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null && user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            existingUser.setEmail(user.getEmail());
        }

        if (user.getRole() != null) {
            if (user.getRole().equals("user") || user.getRole().equals("admin")) {
                existingUser.setRole(user.getRole());
            } else {
                throw new RuntimeException("Invalid role");
            }
        }

        // Handle avatar updates
        if (user.getAvatarBase64() != null && !user.getAvatarBase64().isEmpty()) {
            // Validate image size (max 2MB)
            String base64Data = user.getAvatarBase64();
            
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
            
            // Publish avatar update event for base64 image
            String contentType = "image/jpeg"; // Default content type
            if (user.getAvatarBase64().startsWith("data:image/")) {
                String[] parts = user.getAvatarBase64().split(";");
                if (parts.length > 0) {
                    contentType = parts[0].replace("data:", "");
                }
            }
            avatarEventService.publishAvatarUpdateEvent(id, user.getAvatarBase64(), contentType);
        }



        User updatedUser = userRepo.save(existingUser);
        return UserDTO.builder()
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .role(updatedUser.getRole())
                .avatarMediaId(updatedUser.getAvatarMediaId())
                .build();
    }

    public void delete(String id) {
        User existingUser = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepo.delete(existingUser);
    }
}
