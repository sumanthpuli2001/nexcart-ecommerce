package com.shopsphere.ecommerce.config;

import com.shopsphere.ecommerce.model.*;
import com.shopsphere.ecommerce.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           CouponRepository couponRepository,
                           ReviewRepository reviewRepository,
                           PaymentRepository paymentRepository,
                           OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
        this.reviewRepository = reviewRepository;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed or update ONLY Admin User (Customers register dynamically through the application)
        userRepository.findByEmail("admin@gmail.com").ifPresentOrElse(adminUser -> {
            if (!"Admin".equals(adminUser.getName())) {
                adminUser.setName("Admin");
                userRepository.save(adminUser);
            }
        }, () -> {
            User admin = new User("Admin", "admin@gmail.com", "Admin@123", "+91-9988776655", "Admin HQ, Tech Park, Hyderabad, TS", Role.ROLE_ADMIN);
            userRepository.save(admin);
        });

        // 2. Seed Official Product Categories
        Category electronics = getOrCreateCategory(
                "Electronics & Gadgets",
                "Flagship smartphones, high-performance laptops, wireless audio, and smart gear",
                "https://images.unsplash.com/photo-1498049860654-af1a5c566876?auto=format&fit=crop&w=600&q=80"
        );

        Category fashion = getOrCreateCategory(
                "Fashion & Apparel",
                "Trending street apparel, leather jackets, limited sneakers, and designer accessories",
                "https://images.unsplash.com/photo-1445205170230-053b83016050?auto=format&fit=crop&w=600&q=80"
        );

        Category home = getOrCreateCategory(
                "Home & Appliances",
                "Smart kitchen tech, robotic appliances, ergonomic workstations, and dynamic ambient lighting",
                "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?auto=format&fit=crop&w=600&q=80"
        );

        Category books = getOrCreateCategory(
                "Books & Learning",
                "Software architecture, distributed cloud systems, clean code, and engineering interview guides",
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=600&q=80"
        );

        // 3. Seed Modern & Latest Product Catalog Items (Idempotent - only adds if not already present)

        // --- LATEST ELECTRONICS & GADGETS ---
        createProductIfNotExists(
                "iPhone 16 Pro Max - Natural Titanium (256GB)",
                "Next-gen flagship smartphone engineered with aerospace Grade 5 Titanium, ultra-fast A18 Pro Bionic processor, 48MP Fusion camera system with 5x optical telephoto zoom, 4K 120 fps Dolby Vision recording, and dedicated haptic Camera Control.",
                new BigDecimal("144900.00"),
                25,
                "https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=800&q=80",
                electronics,
                5.0
        );

        createProductIfNotExists(
                "MacBook Pro 16\" Liquid Retina XDR (M3 Max Chip)",
                "Pro powerhouse laptop supercharged by Apple M3 Max 16-core CPU, 40-core GPU, 36GB Unified Memory, 1TB blazing SSD, 120Hz ProMotion XDR display, six-speaker spatial sound, and up to 22 hours battery endurance.",
                new BigDecimal("249990.00"),
                15,
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=800&q=80",
                electronics,
                4.9
        );

        createProductIfNotExists(
                "Sony WH-1000XM5 Wireless Noise-Cancelling Headphones",
                "Flagship wireless headphones featuring dual processors and 8 microphones for unmatched active noise cancellation, LDAC High-Resolution audio, 30-hour playback, multipoint connection, and soft-fit leather cups.",
                new BigDecimal("29990.00"),
                35,
                "https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=800&q=80",
                electronics,
                4.9
        );

        createProductIfNotExists(
                "Apple Watch Ultra 2 GPS + Cellular 49mm Titanium",
                "Rugged endurance smartwatch crafted with lightweight titanium case, sapphire crystal face, ultra-bright 3000-nit display, dual-frequency precision GPS, customized Action button, and 100m water resistance.",
                new BigDecimal("89990.00"),
                20,
                "https://images.unsplash.com/photo-1434493789847-2f02dc6ca35d?auto=format&fit=crop&w=800&q=80",
                electronics,
                4.9
        );

        createProductIfNotExists(
                "DJI Mini 4 Pro 4K HDR Foldable Drone",
                "Ultralight sub-249g camera drone with omnidirectional active obstacle sensing, 4K/60fps HDR True Vertical Shooting, 20km FHD O4 video transmission, and master automated cinematic flight modes.",
                new BigDecimal("89900.00"),
                12,
                "https://images.unsplash.com/photo-1527977966376-1c8408f9f108?auto=format&fit=crop&w=800&q=80",
                electronics,
                4.8
        );

        createProductIfNotExists(
                "Ultra-Slim Smartwatch Pro with AMOLED Display",
                "Advanced fitness tracker smartwatch featuring SpO2 sensor, heart rate monitoring, built-in GPS, and 7-day battery life.",
                new BigDecimal("8999.00"),
                40,
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80",
                electronics,
                4.7
        );

        createProductIfNotExists(
                "4K Ultra HD Waterproof Action Camera",
                "Professional sports action camera with dual color screens, 20MP photo sensor, EIS stabilization, and wide-angle lens.",
                new BigDecimal("12999.00"),
                18,
                "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?auto=format&fit=crop&w=600&q=80",
                electronics,
                4.5
        );

        createProductIfNotExists(
                "Wireless Active Noise-Canceling Headphones",
                "Premium over-ear Bluetooth headphones with active noise cancellation, 30-hour battery life, and spatial audio sound quality.",
                new BigDecimal("14999.00"),
                25,
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80",
                electronics,
                4.9
        );

        // --- LATEST FASHION & APPAREL ---
        createProductIfNotExists(
                "Nike Air Jordan 1 Retro High OG 'Chicago'",
                "Legendary collector high-top basketball sneaker built with vintage cracked leather upper, encapsulated Nike Air-Sole cushioning, iconic wings logo, and signature Chicago colorblocking.",
                new BigDecimal("19999.00"),
                18,
                "https://images.unsplash.com/photo-1552346154-21d32810aba3?auto=format&fit=crop&w=800&q=80",
                fashion,
                5.0
        );

        createProductIfNotExists(
                "Futuristic Minimalist Waterproof Techwear Jacket",
                "High-performance urban techwear jacket engineered with breathable triple-layer GORE-TEX waterproof membrane, Fidlock magnetic utility buckles, articulated sleeves, and seam-sealed storm protection.",
                new BigDecimal("9499.00"),
                30,
                "https://images.unsplash.com/photo-1544441893-675973e31985?auto=format&fit=crop&w=800&q=80",
                fashion,
                4.8
        );

        createProductIfNotExists(
                "Ray-Ban Meta Smart Glasses Wayfarer with AI",
                "Iconic style reimagined with smart technology: capture photos and ultra-wide 1080p video with 12MP camera, discreet open-ear temple speakers, five-mic array, and conversational Meta AI voice control.",
                new BigDecimal("29999.00"),
                22,
                "https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=800&q=80",
                fashion,
                4.7
        );

        createProductIfNotExists(
                "Handcrafted Vintage Leather Bomber Jacket",
                "Authentic genuine lambskin leather jacket with plush thermal inner lining and heavy-duty YKK brass zippers.",
                new BigDecimal("6999.00"),
                12,
                "https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=600&q=80",
                fashion,
                4.9
        );

        createProductIfNotExists(
                "Pro Urban Breathable Running Sneakers",
                "Ultra-lightweight mesh athletic running shoes with high-rebound air cushioning for maximum performance.",
                new BigDecimal("3499.00"),
                50,
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80",
                fashion,
                4.8
        );

        // --- LATEST HOME & APPLIANCES ---
        createProductIfNotExists(
                "Dyson V15 Detect Absolute Smart Cordless Vacuum",
                "Intelligent cleaning system with laser dust illumination revealing invisible microscopic particles, acoustic piezo dirt sensor, LCD real-time scientific proof of deep clean, and 230AW suction.",
                new BigDecimal("65900.00"),
                16,
                "https://images.unsplash.com/photo-1558317374-067fb5f30001?auto=format&fit=crop&w=800&q=80",
                home,
                4.9
        );

        createProductIfNotExists(
                "Philips Hue Dynamic Smart Ambient Light Gradient Bar",
                "Next-level smart entertainment lighting blending multiple colors of light simultaneously in a seamless gradient. Syncs with PC games, movies, and music with 16 million vibrant colors.",
                new BigDecimal("14999.00"),
                40,
                "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=800&q=80",
                home,
                4.8
        );

        createProductIfNotExists(
                "De'Longhi Magnifica S Smart Automatic Espresso Machine",
                "Premium Italian bean-to-cup espresso maker featuring integrated 13-setting burr grinder, 15-bar thermoblock extraction, manual milk frother for velvety cappuccinos, and one-touch aroma buttons.",
                new BigDecimal("42990.00"),
                10,
                "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=800&q=80",
                home,
                4.9
        );

        createProductIfNotExists(
                "Italian Automatic Pump Espresso Coffee Machine",
                "Commercial grade 15-bar Italian pressure espresso maker with integrated milk steam frother and thermoblock heating.",
                new BigDecimal("18999.00"),
                15,
                "https://images.unsplash.com/photo-1570968915860-54d5c301fa9f?auto=format&fit=crop&w=600&q=80",
                home,
                4.8
        );

        createProductIfNotExists(
                "Ergonomic High-Back Executive Office Chair",
                "Breathable mesh ergonomic desk chair with 3D armrests, dynamic lumbar support, and 135-degree tilt recline.",
                new BigDecimal("12499.00"),
                30,
                "https://images.unsplash.com/photo-1592078615290-033ee584e267?auto=format&fit=crop&w=800&q=80",
                home,
                4.6
        );

        // --- LATEST BOOKS & LEARNING ---
        createProductIfNotExists(
                "Designing Data-Intensive Applications by Martin Kleppmann",
                "The essential guide to modern distributed computing architectures, scalability patterns, data storage systems, partition strategies, stream processing, and high reliability cloud design.",
                new BigDecimal("3499.00"),
                50,
                "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?auto=format&fit=crop&w=800&q=80",
                books,
                5.0
        );

        createProductIfNotExists(
                "System Design Interview - An Insider's Guide by Alex Xu",
                "Comprehensive battle-tested preparation for distributed systems interviews: architectural deep dives into rate limiters, distributed key-value stores, cloud storage, and large-scale messaging.",
                new BigDecimal("2799.00"),
                65,
                "https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=800&q=80",
                books,
                4.9
        );

        createProductIfNotExists(
                "Clean Code: A Handbook of Agile Software Craftsmanship",
                "Essential reading for software developers by Robert C. Martin on writing clean, maintainable, and robust code.",
                new BigDecimal("2499.00"),
                60,
                "https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=600&q=80",
                books,
                4.9
        );

        createProductIfNotExists(
                "Design Patterns: Elements of Reusable Software Architecture",
                "The fundamental landmark work on Gang of Four object-oriented architectural patterns and enterprise designs.",
                new BigDecimal("2999.00"),
                45,
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80",
                books,
                4.8
        );

        // 4. Seed Active Promotional Coupons (if not already seeded)
        if (couponRepository.count() == 0) {
            couponRepository.save(new Coupon("WELCOME10", 10, new BigDecimal("500.00"), new BigDecimal("499.00"), LocalDate.now().plusYears(1)));
            couponRepository.save(new Coupon("FESTIVE20", 20, new BigDecimal("1500.00"), new BigDecimal("1999.00"), LocalDate.now().plusYears(1)));
            couponRepository.save(new Coupon("NEXCART50", 50, new BigDecimal("3000.00"), new BigDecimal("4999.00"), LocalDate.now().plusYears(1)));
            couponRepository.save(new Coupon("SAVE100", 15, new BigDecimal("1000.00"), new BigDecimal("999.00"), LocalDate.now().plusYears(1)));
            System.out.println("--> Seeded active promotional coupons (WELCOME10, FESTIVE20, NEXCART50, SAVE100)");
        }

        // 5. Seed Customer Reviews for Flagship Products (if empty)
        if (reviewRepository.count() == 0) {
            User reviewer = userRepository.findByEmail("sumanth@gmail.com")
                    .orElseGet(() -> userRepository.findByEmail("admin@gmail.com").orElse(null));

            if (reviewer != null) {
                productRepository.findAll().forEach(prod -> {
                    if (prod.getName().contains("iPhone 16 Pro Max")) {
                        reviewRepository.save(new Review(5, "Incredible Grade 5 Titanium build and the A18 Pro chip is lightning fast! The Camera Control button feels like shooting with a dedicated mirrorless camera. Easily the best iPhone Apple has produced.", reviewer, prod));
                    } else if (prod.getName().contains("MacBook Pro 16")) {
                        reviewRepository.save(new Review(5, "The M3 Max chip compiles massive enterprise codebases in seconds. 120Hz Liquid Retina display is ultra crisp and battery life lasts all day without throttling.", reviewer, prod));
                    } else if (prod.getName().contains("Sony WH-1000XM5")) {
                        reviewRepository.save(new Review(5, "Active Noise Cancellation silences flights completely. High-res LDAC audio quality is punchy and detailed. Super comfortable for long coding sessions.", reviewer, prod));
                    } else if (prod.getName().contains("Clean Code")) {
                        reviewRepository.save(new Review(5, "Must-read engineering classic. The principles on naming, refactoring, and single-responsibility helped immensely in my software design career.", reviewer, prod));
                    }
                });
                System.out.println("--> Seeded verified customer reviews");
            }
        }

        // 6. Seed Payment Transaction Records for any existing orders
        List<Order> existingOrders = orderRepository.findAll();
        for (Order ord : existingOrders) {
            if (paymentRepository.findByOrder(ord).isEmpty()) {
                String txnId = "TXN-" + System.currentTimeMillis() + "-" + ord.getId() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                paymentRepository.save(new Payment(txnId, ord, ord.getPaymentMethod(), ord.getTotalAmount()));
            }
        }

        System.out.println("--> NexCart Persistent Data Initialized Successfully! Admin: admin@gmail.com / Admin@123");
    }

    private Category getOrCreateCategory(String name, String description, String imageUrl) {
        return categoryRepository.findByName(name).orElseGet(() -> {
            Category category = new Category(name, description, imageUrl);
            return categoryRepository.save(category);
        });
    }

    private void createProductIfNotExists(String name, String description, BigDecimal price, int stock, String imageUrl, Category category, double rating) {
        if (!productRepository.existsByNameIgnoreCase(name.trim())) {
            Product product = new Product(name.trim(), description, price, stock, imageUrl, category, rating);
            productRepository.save(product);
        }
    }
}
