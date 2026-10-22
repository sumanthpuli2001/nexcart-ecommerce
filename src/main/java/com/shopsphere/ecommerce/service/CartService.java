package com.shopsphere.ecommerce.service;

import com.shopsphere.ecommerce.dto.Cart;
import com.shopsphere.ecommerce.model.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private static final String CART_SESSION_KEY = "SHOPPING_CART";

    private final ProductService productService;

    public CartService(ProductService productService) {
        this.productService = productService;
    }

    public Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new Cart();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public void addToCart(HttpSession session, Long productId, int quantity) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        Cart cart = getCart(session);
        cart.addItem(product, quantity);
    }

    public void updateQuantity(HttpSession session, Long productId, int quantity) {
        Cart cart = getCart(session);
        cart.updateQuantity(productId, quantity);
    }

    public void removeFromCart(HttpSession session, Long productId) {
        Cart cart = getCart(session);
        cart.removeItem(productId);
    }

    public void clearCart(HttpSession session) {
        Cart cart = getCart(session);
        cart.clear();
    }
}
