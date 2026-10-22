package com.shopsphere.ecommerce.service;

import com.shopsphere.ecommerce.dto.Cart;
import com.shopsphere.ecommerce.dto.CartItemDto;
import com.shopsphere.ecommerce.dto.CheckoutForm;
import com.shopsphere.ecommerce.model.*;
import com.shopsphere.ecommerce.repository.OrderRepository;
import com.shopsphere.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final com.shopsphere.ecommerce.repository.PaymentRepository paymentRepository;

    public OrderService(OrderRepository orderRepository, 
                        ProductRepository productRepository,
                        com.shopsphere.ecommerce.repository.PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Order placeOrder(User user, Cart cart, CheckoutForm checkoutForm) {
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart.");
        }

        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String fullShippingAddress = checkoutForm.getAddress() + ", " + checkoutForm.getCity() + " - " + checkoutForm.getZipCode();

        Order order = new Order(
                orderNumber,
                user,
                cart.getFinalAmount(),
                fullShippingAddress,
                checkoutForm.getPaymentMethod()
        );
        order.setCouponCode(cart.getCouponCode());
        order.setDiscountAmount(cart.getDiscountAmount());

        for (CartItemDto itemDto : cart.getItems()) {
            // Concurrency-safe pessimistic write lock on the product row
            Product product = productRepository.findByIdWithLock(itemDto.getProduct().getId())
                    .orElseThrow(() -> new IllegalStateException("Product not found ID: " + itemDto.getProduct().getId()));
            
            // Check stock availability
            if (product.getStockQuantity() < itemDto.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for: " + product.getName() + " (Only " + product.getStockQuantity() + " remaining)");
            }

            // Deduct stock under database row lock
            product.setStockQuantity(product.getStockQuantity() - itemDto.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem(product, itemDto.getQuantity(), product.getPrice());
            order.addItem(orderItem);
        }

        order.setStatus(OrderStatus.PROCESSING);
        Order savedOrder = orderRepository.save(order);

        // Record payment transaction
        String txnId = "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Payment payment = new Payment(txnId, savedOrder, checkoutForm.getPaymentMethod(), cart.getFinalAmount());
        paymentRepository.save(payment);

        return savedOrder;
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        
        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == newStatus) {
            return order;
        }

        // Automatic Inventory Restock on Order Cancellation
        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    Product product = productRepository.findByIdWithLock(item.getProduct().getId()).orElse(item.getProduct());
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        } else if (oldStatus == OrderStatus.CANCELLED && newStatus != OrderStatus.CANCELLED) {
            // Re-validate stock if un-cancelling an order
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    Product product = productRepository.findByIdWithLock(item.getProduct().getId()).orElse(item.getProduct());
                    if (product.getStockQuantity() < item.getQuantity()) {
                        throw new IllegalStateException("Cannot reinstate order. Insufficient stock for: " + product.getName());
                    }
                    product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}
