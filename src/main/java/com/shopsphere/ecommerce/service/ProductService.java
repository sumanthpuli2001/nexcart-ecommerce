package com.shopsphere.ecommerce.service;

import com.shopsphere.ecommerce.model.Category;
import com.shopsphere.ecommerce.model.Product;
import com.shopsphere.ecommerce.repository.CategoryRepository;
import com.shopsphere.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchProducts(keyword.trim());
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public List<Product> sortProducts(List<Product> products, String sortBy) {
        if (products == null || sortBy == null || sortBy.trim().isEmpty()) {
            return products;
        }
        List<Product> sorted = new java.util.ArrayList<>(products);
        switch (sortBy.trim().toLowerCase()) {
            case "price_asc":
                sorted.sort(java.util.Comparator.comparing(Product::getPrice));
                break;
            case "price_desc":
                sorted.sort(java.util.Comparator.comparing(Product::getPrice).reversed());
                break;
            case "rating":
                sorted.sort(java.util.Comparator.comparing(Product::getRating, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())).reversed());
                break;
            case "newest":
                sorted.sort(java.util.Comparator.comparing(Product::getId).reversed());
                break;
            default:
                break;
        }
        return sorted;
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Product createProduct(String name, String description, BigDecimal price, Integer stock, String imageUrl, Long categoryId) {
        if (name == null || name.trim().length() < 3) {
            throw new IllegalArgumentException("Product name must be at least 3 characters long.");
        }
        if (productRepository.existsByNameIgnoreCase(name.trim())) {
            throw new IllegalArgumentException("A product named '" + name.trim() + "' already exists in the catalog. Duplicate products are not allowed.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero.");
        }
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        if (imageUrl == null || imageUrl.trim().isEmpty() || (!imageUrl.trim().startsWith("http://") && !imageUrl.trim().startsWith("https://"))) {
            throw new IllegalArgumentException("A valid image URL starting with http:// or https:// is required.");
        }
        if (description == null || description.trim().length() < 10) {
            throw new IllegalArgumentException("Product description must be at least 10 characters long.");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Please select a valid product category."));

        Product product = new Product(name.trim(), description.trim(), price, stock, imageUrl.trim(), category);
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, String name, String description, BigDecimal price, Integer stock, String imageUrl, Long categoryId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        if (name == null || name.trim().length() < 3) {
            throw new IllegalArgumentException("Product name must be at least 3 characters long.");
        }
        if (productRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), id)) {
            throw new IllegalArgumentException("Another product named '" + name.trim() + "' already exists. Duplicate products are not allowed.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero.");
        }
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        if (imageUrl == null || imageUrl.trim().isEmpty() || (!imageUrl.trim().startsWith("http://") && !imageUrl.trim().startsWith("https://"))) {
            throw new IllegalArgumentException("A valid image URL starting with http:// or https:// is required.");
        }
        if (description == null || description.trim().length() < 10) {
            throw new IllegalArgumentException("Product description must be at least 10 characters long.");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Please select a valid product category."));

        product.setName(name.trim());
        product.setDescription(description.trim());
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setImageUrl(imageUrl.trim());
        product.setCategory(category);

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
