package com.shopsphere.ecommerce.controller;

import com.shopsphere.ecommerce.dto.Cart;
import com.shopsphere.ecommerce.dto.CheckoutForm;
import com.shopsphere.ecommerce.model.Coupon;
import com.shopsphere.ecommerce.model.Order;
import com.shopsphere.ecommerce.model.User;
import com.shopsphere.ecommerce.repository.CouponRepository;
import com.shopsphere.ecommerce.service.CartService;
import com.shopsphere.ecommerce.service.OrderService;
import com.shopsphere.ecommerce.service.ProductService;
import com.shopsphere.ecommerce.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final ProductService productService;
    private final CouponRepository couponRepository;
    private final WishlistService wishlistService;

    public OrderController(OrderService orderService,
                           CartService cartService,
                           ProductService productService,
                           CouponRepository couponRepository,
                           WishlistService wishlistService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.productService = productService;
        this.couponRepository = couponRepository;
        this.wishlistService = wishlistService;
    }

    private void populateUserAttributes(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        model.addAttribute("cartItemCount", cartService.getCart(session).getTotalItemsCount());
        model.addAttribute("wishlistCount", loggedInUser != null ? wishlistService.getWishlistCount(loggedInUser) : 0);
    }

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to proceed to checkout.");
            return "redirect:/login";
        }

        Cart cart = cartService.getCart(session);
        if (cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your shopping cart is empty.");
            return "redirect:/cart";
        }

        CheckoutForm checkoutForm = new CheckoutForm();
        checkoutForm.setFullName(loggedInUser.getName());
        checkoutForm.setEmail(loggedInUser.getEmail());
        checkoutForm.setPhone(loggedInUser.getPhone());
        checkoutForm.setAddress(loggedInUser.getAddress());
        checkoutForm.setPaymentMethod("Cash On Delivery");

        model.addAttribute("checkoutForm", checkoutForm);
        model.addAttribute("cart", cart);
        model.addAttribute("availableCoupons", couponRepository.findAll());
        model.addAttribute("categories", productService.getAllCategories());
        populateUserAttributes(model, session);
        return "checkout";
    }

    @PostMapping("/checkout/apply-coupon")
    public String applyCoupon(@RequestParam("code") String code,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Cart cart = cartService.getCart(session);
        if (code == null || code.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("couponError", "Please enter a valid coupon code.");
            return "redirect:/checkout";
        }
        Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCaseAndActiveTrue(code.trim().toUpperCase());
        if (couponOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("couponError", "Invalid or inactive coupon code: " + code);
            return "redirect:/checkout";
        }

        Coupon coupon = couponOpt.get();
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("couponError", "Coupon code '" + coupon.getCode() + "' has expired.");
            return "redirect:/checkout";
        }
        if (coupon.getMinOrderAmount() != null && cart.getTotalAmount().compareTo(coupon.getMinOrderAmount()) < 0) {
            redirectAttributes.addFlashAttribute("couponError", "Minimum order amount of ₹" + coupon.getMinOrderAmount() + " required to use " + coupon.getCode());
            return "redirect:/checkout";
        }

        BigDecimal discount = cart.getTotalAmount()
                .multiply(BigDecimal.valueOf(coupon.getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discount = coupon.getMaxDiscountAmount();
        }

        cart.setCouponCode(coupon.getCode());
        cart.setDiscountAmount(discount);
        redirectAttributes.addFlashAttribute("couponSuccess", "Coupon '" + coupon.getCode() + "' applied! You saved ₹" + discount);
        return "redirect:/checkout";
    }

    @PostMapping("/checkout/remove-coupon")
    public String removeCoupon(HttpSession session, RedirectAttributes redirectAttributes) {
        Cart cart = cartService.getCart(session);
        cart.setCouponCode(null);
        cart.setDiscountAmount(BigDecimal.ZERO);
        redirectAttributes.addFlashAttribute("couponSuccess", "Coupon removed successfully.");
        return "redirect:/checkout";
    }

    @PostMapping("/order/place")
    public String placeOrder(@Valid @ModelAttribute("checkoutForm") CheckoutForm checkoutForm,
                             BindingResult bindingResult,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCart(session);

        if (bindingResult.hasErrors()) {
            model.addAttribute("cart", cart);
            model.addAttribute("categories", productService.getAllCategories());
            populateUserAttributes(model, session);
            return "checkout";
        }

        try {
            Order order = orderService.placeOrder(loggedInUser, cart, checkoutForm);
            cartService.clearCart(session);
            redirectAttributes.addFlashAttribute("placedOrder", order);
            return "redirect:/order/success/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Order placement failed: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/order/success/{id}")
    public String orderSuccess(@PathVariable Long id, Model model, HttpSession session) {
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        model.addAttribute("order", order);
        model.addAttribute("categories", productService.getAllCategories());
        populateUserAttributes(model, session);
        return "order-success";
    }

    @GetMapping("/order/history")
    public String orderHistory(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to view order history.");
            return "redirect:/login";
        }

        List<Order> orders = orderService.getUserOrders(loggedInUser);
        model.addAttribute("orders", orders);
        model.addAttribute("categories", productService.getAllCategories());
        populateUserAttributes(model, session);
        return "order-history";
    }
}
