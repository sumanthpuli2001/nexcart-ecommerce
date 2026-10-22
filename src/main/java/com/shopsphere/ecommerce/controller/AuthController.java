package com.shopsphere.ecommerce.controller;

import com.shopsphere.ecommerce.model.Role;
import com.shopsphere.ecommerce.model.User;
import com.shopsphere.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    public static final String LOGGED_IN_USER_SESSION_KEY = "LOGGED_IN_USER";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam(value = "email", required = false) String email,
                               @RequestParam(value = "password", required = false) String password,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide both email address and password.");
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.authenticate(email, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            session.setAttribute(LOGGED_IN_USER_SESSION_KEY, user);
            redirectAttributes.addFlashAttribute("successMessage", "Welcome back, " + user.getName() + "!");
            if (user.getRole() == Role.ROLE_ADMIN) {
                return "redirect:/admin/dashboard";
            }
            
            String redirectUrl = (String) session.getAttribute("REDIRECT_URL");
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                session.removeAttribute("REDIRECT_URL");
                return "redirect:" + redirectUrl;
            }
            
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid email or password. Please verify your credentials.");
            redirectAttributes.addFlashAttribute("enteredEmail", email);
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam(value = "name", required = false) String name,
                                  @RequestParam(value = "email", required = false) String email,
                                  @RequestParam(value = "password", required = false) String password,
                                  @RequestParam(value = "phone", required = false) String phone,
                                  @RequestParam(value = "address", required = false) String address,
                                  RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(name, email, password, phone, address, Role.ROLE_CUSTOMER);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You can now sign in.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("regName", name);
            redirectAttributes.addFlashAttribute("regEmail", email);
            redirectAttributes.addFlashAttribute("regPhone", phone);
            redirectAttributes.addFlashAttribute("regAddress", address);
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute(LOGGED_IN_USER_SESSION_KEY);
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out.");
        return "redirect:/login";
    }
}
