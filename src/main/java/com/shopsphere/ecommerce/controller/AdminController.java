package com.shopsphere.ecommerce.controller;

import com.shopsphere.ecommerce.model.*;
import com.shopsphere.ecommerce.service.OrderService;
import com.shopsphere.ecommerce.service.ProductService;
import com.shopsphere.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    public AdminController(ProductService productService, OrderService orderService, UserService userService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
    }

    private boolean checkAdminAccess(HttpSession session) {
        User user = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        return user != null && user.getRole() == Role.ROLE_ADMIN;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Admin access required.");
            return "redirect:/login";
        }

        List<Product> products = productService.getAllProducts();
        List<Order> orders = orderService.getAllOrders();
        List<User> users = userService.getAllUsers();

        BigDecimal totalSales = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalProducts", products.size());
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("recentOrders", orders.stream().limit(5).toList());
        model.addAttribute("allProducts", products);
        model.addAttribute("allOrders", orders);
        model.addAttribute("allUsers", users);

        return "admin/dashboard";
    }

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
