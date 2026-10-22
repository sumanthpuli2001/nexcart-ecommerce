package com.shopsphere.ecommerce.dto;

import com.shopsphere.ecommerce.model.Product;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {

    private final List<CartItemDto> items = new ArrayList<>();
    private String couponCode;
    private BigDecimal discountAmount = BigDecimal.ZERO;

    public void addItem(Product product, int quantity) {
        Optional<CartItemDto> existingItem = items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItemDto item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            items.add(new CartItemDto(product, quantity));
        }
    }

    public void updateQuantity(Long productId, int quantity) {
        if (quantity <= 0) {
            removeItem(productId);
            return;
        }

        items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
    }

    public void removeItem(Long productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void clear() {
        items.clear();
        couponCode = null;
        discountAmount = BigDecimal.ZERO;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public int getTotalItemsCount() {
        return items.stream().mapToInt(CartItemDto::getQuantity).sum();
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount != null ? discountAmount : BigDecimal.ZERO;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalAmount() {
        BigDecimal total = getTotalAmount();
        BigDecimal finalAmt = total.subtract(getDiscountAmount());
        return finalAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalAmt;
    }
}
