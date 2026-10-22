package com.shopsphere.ecommerce.service;

import com.shopsphere.ecommerce.model.Product;
import com.shopsphere.ecommerce.model.User;
import com.shopsphere.ecommerce.model.WishlistItem;
import com.shopsphere.ecommerce.repository.ProductRepository;
import com.shopsphere.ecommerce.repository.WishlistItemRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public WishlistService(WishlistItemRepository wishlistItemRepository,
                           ProductRepository productRepository,
                           CartService cartService) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    public List<WishlistItem> getWishlist(User user) {
        if (user == null || user.getId() == null) {
            return Collections.emptyList();
        }
        return wishlistItemRepository.findByUserIdOrderByAddedAtDesc(user.getId());
    }

    public int getWishlistCount(User user) {
        if (user == null || user.getId() == null) {
            return 0;
        }
        return wishlistItemRepository.findByUserIdOrderByAddedAtDesc(user.getId()).size();
    }

    public boolean isInWishlist(User user, Long productId) {
        if (user == null || user.getId() == null || productId == null) {
            return false;
        }
        return wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productId);
    }

    @Transactional
    public boolean toggleWishlist(User user, Long productId) {
        if (user == null || productId == null) {
            return false;
        }
        if (wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            wishlistItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
            return false; // Removed
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
            WishlistItem item = new WishlistItem(user, product);
            wishlistItemRepository.save(item);
            return true; // Added
        }
    }

    @Transactional
    public void removeFromWishlist(User user, Long productId) {
        if (user != null && productId != null) {
            wishlistItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
        }
    }

    @Transactional
    public void moveToCart(User user, Long productId, HttpSession session) {
        if (user == null || productId == null) {
            return;
        }
        cartService.addToCart(session, productId, 1);
        wishlistItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }
}
