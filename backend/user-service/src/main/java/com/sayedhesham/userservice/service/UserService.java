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

    public List<User> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .peek(user -> user.setPassword(null)) // hide password
                .toList();
    }

    public User getById(String id) {
        return userRepo.findById(id)
                .map(user -> {
                    user.setPassword(null);
                    return user;
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User update(String id, UserDTO user) {
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
        updatedUser.setPassword(null);
        return updatedUser;
    }

    public void delete(String id) {
        User existingUser = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepo.delete(existingUser);
    }
}
