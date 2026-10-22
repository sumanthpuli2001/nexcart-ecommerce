package com.shopsphere.ecommerce.service;

import com.shopsphere.ecommerce.model.Category;
import com.shopsphere.ecommerce.model.Product;
import com.shopsphere.ecommerce.model.Role;
import com.shopsphere.ecommerce.model.User;
import com.shopsphere.ecommerce.model.WishlistItem;
import com.shopsphere.ecommerce.repository.ProductRepository;
import com.shopsphere.ecommerce.repository.WishlistItemRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartService cartService;

    @Mock
    private HttpSession session;

    private WishlistService wishlistService;

    private User sampleUser;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        wishlistService = new WishlistService(wishlistItemRepository, productRepository, cartService);
        sampleUser = new User("Sumanth", "sumanth@example.com", "pass", "+919876543210", "Hyderabad", Role.ROLE_CUSTOMER);
        sampleUser.setId(1L);
        sampleProduct = new Product("Smart Watch", "AMOLED display", new BigDecimal("3500.00"), 15, "http://img.jpg", new Category("Electronics", "Gadgets", ""));
        sampleProduct.setId(99L);
    }

    @Test
    @DisplayName("Should return wishlist items for user")
    void shouldGetWishlistItems() {
        WishlistItem item = new WishlistItem(sampleUser, sampleProduct);
        when(wishlistItemRepository.findByUserIdOrderByAddedAtDesc(1L)).thenReturn(List.of(item));

        List<WishlistItem> results = wishlistService.getWishlist(sampleUser);

        assertEquals(1, results.size());
        assertEquals("Smart Watch", results.get(0).getProduct().getName());
        verify(wishlistItemRepository, times(1)).findByUserIdOrderByAddedAtDesc(1L);
    }

    @Test
    @DisplayName("Should toggle item into wishlist when not already present")
    void shouldAddWhenToggledAndNotPresent() {
        when(wishlistItemRepository.existsByUserIdAndProductId(1L, 99L)).thenReturn(false);
        when(productRepository.findById(99L)).thenReturn(Optional.of(sampleProduct));

        boolean added = wishlistService.toggleWishlist(sampleUser, 99L);

        assertTrue(added);
        verify(wishlistItemRepository, times(1)).save(any(WishlistItem.class));
    }

    @Test
    @DisplayName("Should toggle item out of wishlist when already present")
    void shouldRemoveWhenToggledAndAlreadyPresent() {
        when(wishlistItemRepository.existsByUserIdAndProductId(1L, 99L)).thenReturn(true);

        boolean added = wishlistService.toggleWishlist(sampleUser, 99L);

        assertFalse(added);
        verify(wishlistItemRepository, times(1)).deleteByUserIdAndProductId(1L, 99L);
        verify(wishlistItemRepository, never()).save(any(WishlistItem.class));
    }

    @Test
    @DisplayName("Should move item from wishlist to shopping cart")
    void shouldMoveItemToCart() {
        wishlistService.moveToCart(sampleUser, 99L, session);

        verify(cartService, times(1)).addToCart(session, 99L, 1);
        verify(wishlistItemRepository, times(1)).deleteByUserIdAndProductId(1L, 99L);
    }
}
