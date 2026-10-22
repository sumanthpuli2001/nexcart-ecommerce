package com.shopsphere.ecommerce.service;

import com.shopsphere.ecommerce.model.Role;
import com.shopsphere.ecommerce.model.User;
import com.shopsphere.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should successfully register user with +91 normalized phone and BCrypt hashed password")
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("+919876543210")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("$2a$10$hashedPasswordSample");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.registerUser("John Doe", "john@example.com", "Secret@123", "9876543210", "123 MG Road, Bengaluru", Role.ROLE_CUSTOMER);

        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("+919876543210", user.getPhone());
        assertEquals("$2a$10$hashedPasswordSample", user.getPassword());
        verify(passwordEncoder, times(1)).encode("Secret@123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when registering with duplicate email")
    void shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("duplicate@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser("Jane", "duplicate@example.com", "Pass@123", "9876543210", "456 Park St", Role.ROLE_CUSTOMER));

        assertTrue(ex.getMessage().contains("Duplicate accounts are not allowed"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when registering with duplicate phone number")
    void shouldRejectDuplicatePhone() {
        when(userRepository.existsByEmailIgnoreCase("unique@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("+919876543210")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser("Jane", "unique@example.com", "Pass@123", "9876543210", "456 Park St", Role.ROLE_CUSTOMER));

        assertTrue(ex.getMessage().contains("Duplicate phone numbers are not allowed"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should authenticate user with matching password")
    void shouldAuthenticateValidUser() {
        User user = new User("Alice", "alice@example.com", "$2a$10$encoded", "+919876543210", "Address", Role.ROLE_CUSTOMER);
        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPass", "$2a$10$encoded")).thenReturn(true);

        Optional<User> result = userService.authenticate("alice@example.com", "rawPass");

        assertTrue(result.isPresent());
        assertEquals("alice@example.com", result.get().getEmail());
    }
}
