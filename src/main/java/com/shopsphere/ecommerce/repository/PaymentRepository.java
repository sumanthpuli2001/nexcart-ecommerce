package com.shopsphere.ecommerce.repository;

import com.shopsphere.ecommerce.model.Order;
import com.shopsphere.ecommerce.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByTransactionId(String transactionId);
    java.util.List<Payment> findAllByOrderByPaidAtDesc();
}
