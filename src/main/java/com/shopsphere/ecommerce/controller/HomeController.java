package com.shopsphere.ecommerce.controller;

import com.shopsphere.ecommerce.model.Category;
import com.shopsphere.ecommerce.model.Product;
import com.shopsphere.ecommerce.model.Review;
import com.shopsphere.ecommerce.model.User;
import com.shopsphere.ecommerce.repository.ProductRepository;
import com.shopsphere.ecommerce.repository.ReviewRepository;
import com.shopsphere.ecommerce.service.CartService;
import com.shopsphere.ecommerce.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CartService cartService;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public HomeController(ProductService productService,
                          CartService cartService,
                          ReviewRepository reviewRepository,
                          ProductRepository productRepository) {
        this.productService = productService;
        this.cartService = cartService;
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        List<Product> products = productService.getAllProducts();
        List<Category> categories = productService.getAllCategories();
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("cartItemCount", cartService.getCart(session).getTotalItemsCount());
        return "index";
    }

    @GetMapping("/product/{id}")
    public String productDetails(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY) == null) {
            session.setAttribute("REDIRECT_URL", "/product/" + id);
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in to view product details and start shopping!");
            return "redirect:/login";
        }
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found ID: " + id));

        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(id);

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("cartItemCount", cartService.getCart(session).getTotalItemsCount());
        return "product-details";
    }

    @PostMapping("/product/{id}/review")
    public String addReview(@PathVariable Long id,
                            @RequestParam(value = "rating", defaultValue = "5") Integer rating,
                            @RequestParam("comment") String comment,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute(AuthController.LOGGED_IN_USER_SESSION_KEY);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please sign in to write a review.");
            return "redirect:/login";
        }
        Product product = productService.getProductById(id).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found.");
            return "redirect:/";
        }
        if (comment == null || comment.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a review comment.");
            return "redirect:/product/" + id;
        }

        Review review = new Review(rating, comment.trim(), user, product);
        reviewRepository.save(review);

        // Recalculate average rating
        List<Review> allReviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(id);
        double avg = allReviews.stream().mapToInt(Review::getRating).average().orElse(5.0);
        product.setRating(Math.round(avg * 10.0) / 10.0);
        productRepository.save(product);

        redirectAttributes.addFlashAttribute("successMessage", "Thank you! Your verified product review has been submitted.");
        return "redirect:/product/" + id;
    }

    @GetMapping("/category/{id}")
    public String categoryProducts(@PathVariable Long id, Model model, HttpSession session) {
        Category category = productService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found ID: " + id));

        List<Product> products = productService.getProductsByCategory(id);
        
        model.addAttribute("selectedCategory", category);
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("cartItemCount", cartService.getCart(session).getTotalItemsCount());
        return "index";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam("query") String query, Model model, HttpSession session) {
        List<Product> products = productService.searchProducts(query);
        
        model.addAttribute("searchQuery", query);
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("cartItemCount", cartService.getCart(session).getTotalItemsCount());
        return "index";
    }
}
