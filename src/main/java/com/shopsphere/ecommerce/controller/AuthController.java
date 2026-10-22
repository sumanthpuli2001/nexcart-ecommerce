package com.shopsphere.ecommerce.controller;

import com.shopsphere.ecommerce.model.Role;
import com.shopsphere.ecommerce.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    public static final String LOGGED_IN_USER_SESSION_KEY = "LOGGED_IN_USER";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(LOGGED_IN_USER_SESSION_KEY);
            session.invalidate();
        }
        return "redirect:/login?logout=true";
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please verify your credentials.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out securely.");
        }
        return "login";
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
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You can now sign in with your credentials.");
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
}
