package com.shopsphere.ecommerce.dto;

import com.shopsphere.ecommerce.model.Product;
import java.math.BigDecimal;

public class CartItemDto {

    private Product product;
    private int quantity;

    public CartItemDto() {
    }

    public CartItemDto(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        if (product == null || product.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
