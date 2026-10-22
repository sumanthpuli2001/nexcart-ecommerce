# ShopSphere - Java E-Commerce Platform

ShopSphere is a full-stack, medium-level E-Commerce web application built using **Java 17**, **Spring Boot 3**, **Spring Data JPA**, **H2 In-Memory Database**, and **Thymeleaf + Bootstrap 5**.

---

## 🌟 Key Features

1. **Product Catalog & Search**
   - Dynamic product browsing across 4 categories (Electronics, Fashion, Home & Kitchen, Books & Media).
   - Live text search across titles and descriptions.
   - Category filtering & star rating display.

2. **Session-Based Shopping Cart**
   - Real-time stock verification.
   - Dynamic subtotal, tax calculation, and item quantity updates.

3. **Checkout & Order Management**
   - Checkout flow with shipping address input & simulated payment options (Card, UPI, COD).
   - Order tracking with unique order IDs (`ORD-XXXXX`).
   - Customer Order History page.

4. **Admin Management Dashboard**
   - Executive metrics panel (Total Sales, Total Orders, Total Products, Total Users).
   - Full CRUD operations for Products (Add, Edit, Delete).
   - Order Status Management (Update order status to `PROCESSING`, `SHIPPED`, or `DELIVERED`).

5. **RESTful APIs**
   - `/api/products` - GET JSON list of products.
   - `/api/orders` - GET JSON list of orders.
   - `/api/orders/summary` - GET JSON summary metrics.

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Backend Framework** | Java 17, Spring Boot 3.2.4 |
| **Persistence / ORM** | Spring Data JPA / Hibernate |
| **Database** | H2 In-Memory Database (Web Console at `/h2-console`) |
| **Frontend View Engine** | Thymeleaf HTML5, CSS3, JavaScript |
| **UI Components** | Bootstrap 5.3, FontAwesome 6 |
| **Build Tool** | Apache Maven 3.9.6 |

---

## 🚀 How to Run the Project

### Option 1: Using the provided `run.bat` script
Double click `run.bat` or execute in PowerShell/CMD:
```powershell
.\run.bat
```

### Option 2: Using Portable Maven Command
```powershell
& "C:\Users\Sumanth Puli\.gemini\antigravity\scratch\mvn\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
```

Once started, open your web browser to:
- **Storefront App:** [http://localhost:8080](http://localhost:8080)
- **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:mem:shopspheredb`
  - Username: `sa`
  - Password: `password`

---

## 🔑 Demo Account Credentials

| User Type | Email | Password | Role |
|---|---|---|---|
| **Customer** | `john@example.com` | `user123` | Customer Storefront |
| **Admin** | `admin@shopsphere.com` | `admin123` | Admin Dashboard |

---

## 📁 Project Architecture

```
ecommerce-java/
├── src/main/java/com/shopsphere/ecommerce/
│   ├── EcommerceApplication.java (Main Entry Point)
│   ├── config/ (Database Seeding DataInitializer)
│   ├── controller/ (MVC Controllers & REST API Controllers)
│   ├── dto/ (Cart DTO, CartItem DTO, Checkout Form)
│   ├── model/ (JPA Entities: User, Product, Category, Order, OrderItem)
│   ├── repository/ (Spring Data JPA Repositories)
│   └── service/ (Business Logic Services)
└── src/main/resources/
    ├── application.properties
    ├── static/ (CSS & JavaScript)
    └── templates/ (Thymeleaf HTML Views & Admin Views)
```
