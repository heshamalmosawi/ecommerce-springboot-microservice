package com.sayedhesham.userservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sayedhesham.userservice.dto.UserDTO;
import com.sayedhesham.userservice.model.User;
import com.sayedhesham.userservice.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepo;

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

    public UserDTO update(String id, UserDTO user) {
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
