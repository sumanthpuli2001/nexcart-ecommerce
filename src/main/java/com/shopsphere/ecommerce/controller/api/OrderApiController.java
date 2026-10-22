package com.shopsphere.ecommerce.controller.api;

import com.shopsphere.ecommerce.model.Order;
import com.shopsphere.ecommerce.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
        return orderService.getOrderByNumber(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getOrdersSummary() {
        List<Order> orders = orderService.getAllOrders();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrdersCount", orders.size());
        stats.put("pendingOrders", orders.stream().filter(o -> o.getStatus().name().equals("PENDING")).count());
        stats.put("processingOrders", orders.stream().filter(o -> o.getStatus().name().equals("PROCESSING")).count());
        stats.put("shippedOrders", orders.stream().filter(o -> o.getStatus().name().equals("SHIPPED")).count());
        stats.put("deliveredOrders", orders.stream().filter(o -> o.getStatus().name().equals("DELIVERED")).count());
        return ResponseEntity.ok(stats);
    }
}
