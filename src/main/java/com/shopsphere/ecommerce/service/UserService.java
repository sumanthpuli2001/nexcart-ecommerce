package com.shopsphere.ecommerce.service;

import com.shopsphere.ecommerce.model.Role;
import com.shopsphere.ecommerce.model.User;
import com.shopsphere.ecommerce.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        
        String formattedPhone = normalizeAndValidatePhone(phone);

        if (userRepository.existsByPhone(formattedPhone)) {
            throw new IllegalArgumentException("This mobile number (" + formattedPhone + ") is already registered. Duplicate phone numbers are not allowed.");
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (address == null || address.trim().length() < 5) {
            throw new IllegalArgumentException("Please provide a complete shipping address (minimum 5 characters).");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(name.trim(), email.trim().toLowerCase(), hashedPassword, formattedPhone, address.trim(), role != null ? role : Role.ROLE_CUSTOMER);
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(email.trim())
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()) || rawPassword.equals(u.getPassword()));
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
            user.setName(name.trim());
        }
        if (phone != null && !phone.isBlank()) {
            String formattedPhone = normalizeAndValidatePhone(phone);
            Optional<User> existingWithPhone = userRepository.findByPhone(formattedPhone);
            if (existingWithPhone.isPresent() && !existingWithPhone.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Mobile number (" + formattedPhone + ") is already in use by another account.");
            }
            user.setPhone(formattedPhone);
        }
        if (address != null && !address.isBlank()) {
            user.setAddress(address.trim());
        }
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 6) {
                throw new IllegalArgumentException("New password must be at least 6 characters long.");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        return userRepository.save(user);
    }

    public String normalizeAndValidatePhone(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("91") && digits.length() == 12) {
            digits = digits.substring(2);
        }
        if (digits.length() != 10) {
            throw new IllegalArgumentException("Please enter a valid 10-digit mobile number.");
        }
        return "+91-" + digits;
    }
}
