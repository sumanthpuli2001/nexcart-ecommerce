package com.shopsphere.ecommerce.service;

import com.shopsphere.ecommerce.model.Role;
import com.shopsphere.ecommerce.model.User;
import com.shopsphere.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String name, String email, String password, String phone, String address, Role role) {
        if (name == null || name.trim().length() < 2) {
            throw new IllegalArgumentException("Full name is required (minimum 2 characters).");
        }
        if (email == null || !email.trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Please enter a valid email address (e.g. name@domain.com).");
        }
        if (userRepository.existsByEmailIgnoreCase(email.trim())) {
            throw new IllegalArgumentException("An account with this email address already exists. Duplicate accounts are not allowed.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Mobile number is required.");
        }
        
        // Extract the 10-digit mobile number and format with +91-
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("91") && digits.length() == 12) {
            digits = digits.substring(2);
        }
        if (digits.length() != 10) {
            throw new IllegalArgumentException("Please enter a valid 10-digit mobile number.");
        }
        String formattedPhone = "+91-" + digits;

        if (userRepository.existsByPhone(formattedPhone)) {
            throw new IllegalArgumentException("This mobile number (" + formattedPhone + ") is already registered. Duplicate phone numbers are not allowed.");
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (address == null || address.trim().length() < 5) {
            throw new IllegalArgumentException("Please provide a complete shipping address (minimum 5 characters).");
        }

        User user = new User(name.trim(), email.trim().toLowerCase(), password, formattedPhone, address.trim(), role != null ? role : Role.ROLE_CUSTOMER);
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(email.trim())
                .filter(u -> u.getPassword().equals(password));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateProfile(Long userId, String name, String phone, String address, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (phone != null && !phone.isBlank()) {
            user.setPhone(phone);
        }
        if (address != null && !address.isBlank()) {
            user.setAddress(address);
        }
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(newPassword);
        }
        return userRepository.save(user);
    }
}
