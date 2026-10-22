package com.shopsphere.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
        System.out.println("==================================================");
        System.out.println("  NexCart E-Commerce Official Platform Started!");
        System.out.println("  URL: http://localhost:8081");
        System.out.println("  Admin Login: admin@gmail.com / Admin@123");
        System.out.println("==================================================");
    }
}
