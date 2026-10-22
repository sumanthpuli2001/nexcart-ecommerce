package com.shopsphere.ecommerce.repository;

import com.shopsphere.ecommerce.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
    long countByProductId(Long productId);
}
