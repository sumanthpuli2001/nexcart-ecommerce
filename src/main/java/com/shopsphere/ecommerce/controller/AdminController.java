package com.shopsphere.ecommerce.controller;

import com.shopsphere.ecommerce.model.*;
import com.shopsphere.ecommerce.repository.CategoryRepository;
import com.shopsphere.ecommerce.repository.CouponRepository;
import com.shopsphere.ecommerce.repository.PaymentRepository;
import com.shopsphere.ecommerce.repository.ProductRepository;
import com.shopsphere.ecommerce.service.OrderService;
import com.shopsphere.ecommerce.service.ProductService;
import com.shopsphere.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final CouponRepository couponRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;

    public AdminController(ProductService productService,
                           OrderService orderService,
                           UserService userService,
                           CategoryRepository categoryRepository,
                           CouponRepository couponRepository,
                           PaymentRepository paymentRepository,
                           ProductRepository productRepository) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.couponRepository = couponRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
    }

    private boolean checkAdminAccess(HttpSession session) {
        User user = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        return user != null && user.getRole() == Role.ROLE_ADMIN;
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Admin access required.");
            return "redirect:/login";
        }

        List<Product> products = productService.getAllProducts();
        List<Order> orders = orderService.getAllOrders();
        List<User> users = userService.getAllUsers();
        List<Category> categories = categoryRepository.findAll();
        List<Coupon> coupons = couponRepository.findAll();

        BigDecimal totalSales = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long lowStockCount = products.stream().filter(p -> p.getStockQuantity() <= 5).count();

        model.addAttribute("totalProducts", products.size());
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalCategories", categories.size());
        model.addAttribute("totalCoupons", coupons.size());
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("recentOrders", orders.stream().limit(5).toList());
        model.addAttribute("allProducts", products);
        model.addAttribute("allOrders", orders);
        model.addAttribute("allUsers", users);

        return "admin/dashboard";
    }

    // ==================== PRODUCT MANAGEMENT ====================

    @GetMapping("/products")
    public String listProducts(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        model.addAttribute("products", productService.getAllProducts());
        return "admin/products";
    }

    @GetMapping("/products/add")
    public String showAddProductForm(Model model, HttpSession session) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.getAllCategories());
        return "admin/add-product";
    }

    @PostMapping("/products/add")
    public String processAddProduct(@RequestParam("name") String name,
                                    @RequestParam("description") String description,
                                    @RequestParam("price") BigDecimal price,
                                    @RequestParam("stockQuantity") Integer stockQuantity,
                                    @RequestParam("imageUrl") String imageUrl,
                                    @RequestParam("categoryId") Long categoryId,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        try {
            productService.createProduct(name, description, price, stockQuantity, imageUrl, categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Product created successfully!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products/add";
        }
    }

    @RequestMapping(value = "/products/edit", method = {RequestMethod.GET, RequestMethod.POST})
    public String showEditProductForm(@RequestParam(value = "id", required = false) Long id,
                                      HttpSession session,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        if (id == null) {
            id = (Long) session.getAttribute("EDIT_PRODUCT_ID");
        }
        if (id == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "No product selected for editing.");
            return "redirect:/admin/products";
        }
        session.setAttribute("EDIT_PRODUCT_ID", id);

        final Long targetId = id;
        Product product = productService.getProductById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + targetId));

        model.addAttribute("product", product);
        model.addAttribute("categories", productService.getAllCategories());
        return "admin/edit-product";
    }

    @GetMapping("/products/edit/{id}")
    public String showEditProductFormLegacy(@PathVariable Long id, HttpSession session) {
        session.setAttribute("EDIT_PRODUCT_ID", id);
        return "redirect:/admin/products/edit";
    }

    @PostMapping("/products/update")
    public String processEditProduct(@RequestParam("id") Long id,
                                     @RequestParam("name") String name,
                                     @RequestParam("description") String description,
                                     @RequestParam("price") BigDecimal price,
                                     @RequestParam("stockQuantity") Integer stockQuantity,
                                     @RequestParam("imageUrl") String imageUrl,
                                     @RequestParam("categoryId") Long categoryId,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        try {
            productService.updateProduct(id, name, description, price, stockQuantity, imageUrl, categoryId);
            session.removeAttribute("EDIT_PRODUCT_ID");
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products/edit?id=" + id;
        }
    }

    @PostMapping("/products/edit/{id}")
    public String processEditProductLegacy(@PathVariable Long id,
                                           @RequestParam("name") String name,
                                           @RequestParam("description") String description,
                                           @RequestParam("price") BigDecimal price,
                                           @RequestParam("stockQuantity") Integer stockQuantity,
                                           @RequestParam("imageUrl") String imageUrl,
                                           @RequestParam("categoryId") Long categoryId,
                                           HttpSession session,
                                           RedirectAttributes redirectAttributes) {
        return processEditProduct(id, name, description, price, stockQuantity, imageUrl, categoryId, session, redirectAttributes);
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProductPost(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        return deleteProduct(id, session, redirectAttributes);
    }

    // ==================== CATEGORY MANAGEMENT ====================

    @GetMapping("/categories")
    public String listCategories(Model model, HttpSession session) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        List<Category> categories = categoryRepository.findAll();
        Map<Long, Long> productCounts = new HashMap<>();
        for (Category cat : categories) {
            productCounts.put(cat.getId(), productRepository.countByCategoryId(cat.getId()));
        }
        model.addAttribute("categories", categories);
        model.addAttribute("productCounts", productCounts);
        return "admin/categories";
    }

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam("name") String name,
                              @RequestParam(value = "description", required = false) String description,
                              @RequestParam(value = "imageUrl", required = false) String imageUrl,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        if (name == null || name.trim().length() < 2) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category name must be at least 2 characters.");
            return "redirect:/admin/categories";
        }
        if (categoryRepository.existsByNameIgnoreCase(name.trim())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category '" + name.trim() + "' already exists.");
            return "redirect:/admin/categories";
        }

        Category category = new Category(name.trim(), description != null ? description.trim() : "", imageUrl != null ? imageUrl.trim() : "");
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("successMessage", "Category '" + category.getName() + "' created successfully!");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/edit")
    public String editCategory(@RequestParam("id") Long id,
                               @RequestParam("name") String name,
                               @RequestParam(value = "description", required = false) String description,
                               @RequestParam(value = "imageUrl", required = false) String imageUrl,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

        if (name == null || name.trim().length() < 2) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category name must be at least 2 characters.");
            return "redirect:/admin/categories";
        }
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Another category named '" + name.trim() + "' already exists.");
            return "redirect:/admin/categories";
        }

        category.setName(name.trim());
        category.setDescription(description != null ? description.trim() : "");
        category.setImageUrl(imageUrl != null ? imageUrl.trim() : "");
        categoryRepository.save(category);

        redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        long count = productRepository.countByCategoryId(id);
        if (count > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete category: it still has " + count + " linked product(s). Reassign or delete products first.");
            return "redirect:/admin/categories";
        }
        categoryRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
        return "redirect:/admin/categories";
    }

    // ==================== COUPON MANAGEMENT ====================

    @GetMapping("/coupons")
    public String listCoupons(Model model, HttpSession session) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        List<Coupon> coupons = couponRepository.findAll();
        model.addAttribute("coupons", coupons);
        return "admin/coupons";
    }

    @PostMapping("/coupons/add")
    public String addCoupon(@RequestParam("code") String code,
                            @RequestParam("discountPercent") Integer discountPercent,
                            @RequestParam(value = "maxDiscountAmount", required = false) BigDecimal maxDiscountAmount,
                            @RequestParam(value = "minOrderAmount", required = false) BigDecimal minOrderAmount,
                            @RequestParam(value = "expiryDate", required = false) String expiryDateStr,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        if (code == null || code.trim().length() < 3) {
            redirectAttributes.addFlashAttribute("errorMessage", "Coupon code must be at least 3 characters.");
            return "redirect:/admin/coupons";
        }
        String formattedCode = code.trim().toUpperCase();
        if (couponRepository.existsByCodeIgnoreCase(formattedCode)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Coupon code '" + formattedCode + "' already exists.");
            return "redirect:/admin/coupons";
        }
        if (discountPercent == null || discountPercent < 1 || discountPercent > 90) {
            redirectAttributes.addFlashAttribute("errorMessage", "Discount percentage must be between 1% and 90%.");
            return "redirect:/admin/coupons";
        }

        LocalDate expiryDate = null;
        if (expiryDateStr != null && !expiryDateStr.trim().isEmpty()) {
            try {
                expiryDate = LocalDate.parse(expiryDateStr.trim());
            } catch (Exception ignored) {}
        }

        Coupon coupon = new Coupon(formattedCode, discountPercent, maxDiscountAmount, minOrderAmount, expiryDate);
        couponRepository.save(coupon);
        redirectAttributes.addFlashAttribute("successMessage", "Coupon '" + formattedCode + "' created successfully!");
        return "redirect:/admin/coupons";
    }

    @PostMapping("/coupons/toggle/{id}")
    public String toggleCoupon(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found with ID: " + id));
        coupon.setActive(!coupon.isActive());
        couponRepository.save(coupon);
        redirectAttributes.addFlashAttribute("successMessage", "Coupon '" + coupon.getCode() + "' status changed to " + (coupon.isActive() ? "Active" : "Inactive"));
        return "redirect:/admin/coupons";
    }

    @PostMapping("/coupons/delete/{id}")
    public String deleteCoupon(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        couponRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Coupon deleted successfully!");
        return "redirect:/admin/coupons";
    }

    // ==================== PAYMENT & FINANCIAL LEDGER ====================

    @GetMapping("/payments")
    public String viewPaymentLedger(Model model, HttpSession session) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        List<Payment> payments = paymentRepository.findAllByOrderByPaidAtDesc();
        BigDecimal totalCollected = payments.stream()
                .filter(p -> "COMPLETED".equalsIgnoreCase(p.getPaymentStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgOrderValue = payments.isEmpty() ? BigDecimal.ZERO :
                totalCollected.divide(BigDecimal.valueOf(payments.size()), 2, RoundingMode.HALF_UP);

        model.addAttribute("payments", payments);
        model.addAttribute("totalCollected", totalCollected);
        model.addAttribute("totalTransactions", payments.size());
        model.addAttribute("avgOrderValue", avgOrderValue);
        return "admin/payments";
    }

    // ==================== ORDER MANAGEMENT ====================

    @GetMapping("/orders")
    public String manageOrders(Model model, HttpSession session) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "admin/orders";
    }

    @PostMapping("/orders/status")
    public String updateOrderStatus(@RequestParam("orderId") Long orderId,
                                    @RequestParam("status") OrderStatus status,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            return "redirect:/login";
        }
        orderService.updateOrderStatus(orderId, status);
        redirectAttributes.addFlashAttribute("successMessage", "Order #" + orderId + " status updated to " + status);
        return "redirect:/admin/orders";
    }

    // ==================== CUSTOMER MANAGEMENT ====================

    @GetMapping("/users")
    public String listCustomerDetails(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Admin access required.");
            return "redirect:/login";
        }
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }
}
