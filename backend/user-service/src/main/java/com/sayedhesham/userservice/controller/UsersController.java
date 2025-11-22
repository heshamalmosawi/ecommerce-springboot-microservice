package com.sayedhesham.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sayedhesham.userservice.dto.UserDTO;
import com.sayedhesham.userservice.dto.UserPatchDTO;
import com.sayedhesham.userservice.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/")
public class UsersController {

    @Autowired
    private UserService userService;

    @GetMapping("/authenticate")
    public ResponseEntity<UserDTO> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            UserDTO user = userService.getById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PatchMapping("/")
    public ResponseEntity<Object> updateMyProfile(@Valid @RequestBody UserPatchDTO user) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication.getName();
            UserDTO updatedUser = userService.update(userId, user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException r) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + r.getMessage());
        } catch (Exception e) {
            System.err.println("UserController: Error updating user - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

}
