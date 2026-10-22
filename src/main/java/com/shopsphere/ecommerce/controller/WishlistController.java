package com.shopsphere.ecommerce.controller;

import com.shopsphere.ecommerce.model.User;
import com.shopsphere.ecommerce.model.WishlistItem;
import com.shopsphere.ecommerce.service.CartService;
import com.shopsphere.ecommerce.service.ProductService;
import com.shopsphere.ecommerce.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WishlistController {

    private final WishlistService wishlistService;
    private final ProductService productService;
    private final CartService cartService;

    public WishlistController(WishlistService wishlistService,
                              ProductService productService,
                              CartService cartService) {
        this.wishlistService = wishlistService;
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping("/wishlist")
    public String viewWishlist(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in to view your wishlist!");
            return "redirect:/login";
        }

        List<WishlistItem> wishlistItems = wishlistService.getWishlist(loggedInUser);
        model.addAttribute("wishlistItems", wishlistItems);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("cartItemCount", cartService.getCart(session).getTotalItemsCount());
        model.addAttribute("wishlistCount", wishlistItems.size());
        return "wishlist";
    }

    @PostMapping("/wishlist/toggle")
    @ResponseBody
    public Map<String, Object> toggleWishlistAjax(@RequestParam("productId") Long productId,
                                                  HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User loggedInUser = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        if (loggedInUser == null) {
            response.put("success", false);
            response.put("message", "Please sign in to save items to your wishlist.");
            return response;
        }

        boolean added = wishlistService.toggleWishlist(loggedInUser, productId);
        int newCount = wishlistService.getWishlistCount(loggedInUser);
        response.put("success", true);
        response.put("added", added);
        response.put("count", newCount);
        response.put("message", added ? "Product saved to your wishlist!" : "Product removed from wishlist.");
        return response;
    }

    @PostMapping("/wishlist/add")
    public String addToWishlist(@RequestParam("productId") Long productId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in to add to your wishlist!");
            return "redirect:/login";
        }

        wishlistService.toggleWishlist(loggedInUser, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Item updated in your wishlist!");
        return "redirect:/wishlist";
    }

    @PostMapping("/wishlist/remove")
    public String removeFromWishlist(@RequestParam("productId") Long productId,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        wishlistService.removeFromWishlist(loggedInUser, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from wishlist.");
        return "redirect:/wishlist";
    }

    @PostMapping("/wishlist/move-to-cart")
    public String moveToCart(@RequestParam("productId") Long productId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            wishlistService.moveToCart(loggedInUser, productId, session);
            redirectAttributes.addFlashAttribute("successMessage", "Item moved to your shopping cart!");
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/wishlist";
        }
    }
}
