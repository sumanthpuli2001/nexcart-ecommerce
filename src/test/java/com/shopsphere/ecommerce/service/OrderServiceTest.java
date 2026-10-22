package com.shopsphere.ecommerce.service;

import com.shopsphere.ecommerce.dto.Cart;
import com.shopsphere.ecommerce.dto.CheckoutForm;
import com.shopsphere.ecommerce.model.*;
import com.shopsphere.ecommerce.repository.OrderRepository;
import com.shopsphere.ecommerce.repository.PaymentRepository;
import com.shopsphere.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, productRepository, paymentRepository);
    }

    @Test
    @DisplayName("Should successfully place order, deduct inventory, and record payment transaction")
    void shouldPlaceOrderAndDeductStock() {
        User user = new User("Sumanth", "sumanth@example.com", "pass", "+919876543210", "Hyderabad", Role.ROLE_CUSTOMER);
        Category category = new Category("Electronics", "Gadgets", "");
        Product product = new Product("Smartphone", "Latest model", new BigDecimal("15000.00"), 10, "http://img.jpg", category);
        product.setId(101L);

        when(productRepository.findByIdWithLock(101L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart cart = new Cart();
        cart.addItem(product, 2);

        CheckoutForm form = new CheckoutForm();
        form.setFullName("Sumanth Puli");
        form.setEmail("sumanth@example.com");
        form.setPhone("+919876543210");
        form.setAddress("Hitech City");
        form.setCity("Hyderabad");
        form.setZipCode("500081");
        form.setPaymentMethod("Cash On Delivery");

        Order placedOrder = orderService.placeOrder(user, cart, form);

        assertNotNull(placedOrder);
        assertEquals(8, product.getStockQuantity()); // 10 - 2 = 8
        assertEquals(1, placedOrder.getItems().size());
        assertEquals("Smartphone", placedOrder.getItems().get(0).getProductName());
        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should fail with IllegalStateException when placing order with insufficient stock")
    void shouldRejectOrderWhenStockInsufficient() {
        User user = new User("Sumanth", "sumanth@example.com", "pass", "+919876543210", "Hyderabad", Role.ROLE_CUSTOMER);
        Category category = new Category("Electronics", "Gadgets", "");
        Product product = new Product("Laptop", "Fast", new BigDecimal("50000.00"), 1, "http://img.jpg", category);
        product.setId(202L);

        when(productRepository.findByIdWithLock(202L)).thenReturn(Optional.of(product));

        Cart cart = new Cart();
        cart.addItem(product, 5); // Requested 5, stock only 1

        CheckoutForm form = new CheckoutForm();
        form.setFullName("Sumanth");
        form.setAddress("Street");
        form.setCity("City");
        form.setZipCode("500001");
        form.setPaymentMethod("Cash On Delivery");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                orderService.placeOrder(user, cart, form));

        assertTrue(ex.getMessage().contains("Insufficient stock"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should automatically restore stock when order is cancelled")
    void shouldRestoreStockOnOrderCancellation() {
        Product product = new Product("Headphones", "Noise cancelling", new BigDecimal("2000.00"), 5, "http://img.jpg", null);
        product.setId(303L);

        User user = new User("Bob", "bob@example.com", "pass", "+919876543210", "Delhi", Role.ROLE_CUSTOMER);
        Order order = new Order("ORD-TEST12", user, new BigDecimal("4000.00"), "Delhi", "Cash On Delivery");
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem(product, 2, new BigDecimal("2000.00"));
        order.addItem(item);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(productRepository.findByIdWithLock(303L)).thenReturn(Optional.of(product));

        orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(7, product.getStockQuantity()); // 5 + 2 = 7 restored!
        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(1)).save(order);
    }
}
