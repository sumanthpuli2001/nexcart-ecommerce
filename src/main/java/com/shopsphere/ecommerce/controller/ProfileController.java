package com.shopsphere.ecommerce.controller;

import com.shopsphere.ecommerce.model.User;
import com.shopsphere.ecommerce.service.CartService;
import com.shopsphere.ecommerce.service.ProductService;
import com.shopsphere.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserService userService;
    private final CartService cartService;
    private final ProductService productService;

    public ProfileController(UserService userService, CartService cartService, ProductService productService) {
        this.userService = userService;
        this.cartService = cartService;
        this.productService = productService;
    }

    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        if (sessionUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in to view your profile details.");
            return "redirect:/login";
        }

        User latestUser = userService.findById(sessionUser.getId())
                .orElse(sessionUser);
        session.setAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY, latestUser);

        model.addAttribute("user", latestUser);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("cartItemCount", cartService.getCart(session).getTotalItemsCount());
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("name") String name,
                                @RequestParam("phone") String phone,
                                @RequestParam("address") String address,
                                @RequestParam(value = "password", required = false) String password,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        if (sessionUser == null) {
            return "redirect:/login";
        }

        try {
            User updatedUser = userService.updateProfile(sessionUser.getId(), name, phone, address, password);
            session.setAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY, updatedUser);
            redirectAttributes.addFlashAttribute("successMessage", "Your profile details have been updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}
