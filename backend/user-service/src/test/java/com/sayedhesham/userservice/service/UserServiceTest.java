package com.sayedhesham.userservice.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sayedhesham.userservice.dto.UserDTO;
import com.sayedhesham.userservice.dto.UserPatchDTO;
import com.sayedhesham.userservice.model.User;
import com.sayedhesham.userservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private AvatarEventService avatarEventService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("1")
                .name("John Doe")
                .email("john@example.com")
                .role("user")
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnListOfUserDTOs() {
        List<User> users = Arrays.asList(testUser);
        when(userRepo.findAll()).thenReturn(users);

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertNotNull(result.get(0));
    }

    @Test
    void getById_WhenUserExists_ShouldReturnUserDTO() {
        when(userRepo.findById("1")).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getById("1");

        assertNotNull(result);
    }

    @Test
    void getById_WhenUserNotExists_ShouldThrowException() {
        when(userRepo.findById("999")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getById("999"));
    }

    @Test
    void update_WhenUserExists_ShouldUpdateUser() {
        UserPatchDTO updateData = UserPatchDTO.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .build();

        when(userRepo.findById("1")).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.update("1", updateData);

        assertNotNull(result);
        verify(userRepo).save(any(User.class));
    }

    @Test
    void delete_WhenUserExists_ShouldDeleteUser() {
        when(userRepo.findById("1")).thenReturn(Optional.of(testUser));

        userService.delete("1");

        verify(userRepo).delete(testUser);
    }

    @Test
    void delete_WhenUserNotExists_ShouldThrowException() {
        when(userRepo.findById("999")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.delete("999"));
    }
}