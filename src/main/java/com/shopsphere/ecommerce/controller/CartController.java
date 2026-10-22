package com.shopsphere.ecommerce.controller;

import com.shopsphere.ecommerce.dto.Cart;
import com.shopsphere.ecommerce.model.User;
import com.shopsphere.ecommerce.service.CartService;
import com.shopsphere.ecommerce.service.ProductService;
import com.shopsphere.ecommerce.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

    private final CartService cartService;
    private final ProductService productService;
    private final WishlistService wishlistService;

    public CartController(CartService cartService,
                          ProductService productService,
                          WishlistService wishlistService) {
        this.cartService = cartService;
        this.productService = productService;
        this.wishlistService = wishlistService;
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in to view your shopping cart!");
            return "redirect:/login";
        }
        Cart cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("cartItemCount", cart.getTotalItemsCount());
        model.addAttribute("wishlistCount", wishlistService.getWishlistCount(loggedInUser));
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY) == null) {
            session.setAttribute("REDIRECT_URL", "/product/" + productId);
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in to add products to your cart!");
            return "redirect:/login";
        }
        try {
            cartService.addToCart(session, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to your shopping cart!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 HttpSession session) {
        cartService.updateQuantity(session, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCartPost(@RequestParam("productId") Long productId,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(session, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from shopping cart.");
        return "redirect:/cart";
    }

    @GetMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(session, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from shopping cart.");
        return "redirect:/cart";
    }
}
