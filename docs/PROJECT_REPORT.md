# Project Report – ShopEase Online Shopping Platform

**Task ID:** JV-EC-001 · **Student code:** DAS-JV-001 · **Company:** Data Alcott Systems  
**Name:** _your name_ · **GitHub:** _link_ · **Live demo:** _link_ · **Video:** _link_

## 1. Introduction
ShopEase is a full-stack e-commerce web application. Customers can register, browse and search a product catalog, manage a shopping cart, place orders with a simulated payment, and follow order status. Administrators manage products, categories, orders and users from a dashboard. The goal was to build a complete, maintainable Spring Boot application following MVC and layered-architecture practices.

## 2. Technology stack
| Layer | Technology | Why |
|---|---|---|
| Framework | Spring Boot 3.3, Spring MVC | Convention-based setup, embedded server |
| Security | Spring Security 6 | Form login, BCrypt, role-based authorization, CSRF |
| Persistence | Spring Data JPA, Hibernate | Entity mapping, derived queries, Specifications for dynamic filters |
| Database | MySQL 8 | Relational integrity for orders and inventory |
| View | Thymeleaf | Server-side templates with Spring Security integration |
| Build / test | Maven, JUnit 5, MockMvc, H2 | Repeatable builds; tests run without MySQL |

## 3. Architecture
```
Browser ──HTTP──▶ Spring Security filter chain (auth, roles, CSRF)
                      │
                 Controllers  (AuthController, ProductController, CartController,
                      │        OrderController, AdminController)
                      │  DTOs + validation
                 Services     (UserService, ProductService, CartService,
                      │        OrderService, PaymentService, EmailService)
                      │  @Transactional
                 Repositories (Spring Data JPA / Hibernate)
                      │
                    MySQL
                 Thymeleaf templates render the model back to the browser
```
**Request example – placing an order:** `POST /checkout` → security checks the session → `OrderController` validates `OrderDto` → `OrderService.placeOrder` opens one transaction: reads the cart, checks and decrements stock, copies prices into `order_items`, charges via `PaymentService`, saves the order, clears the cart → redirect to the order page.

## 4. Database design
| Table | Purpose | Key fields |
|---|---|---|
| users | Accounts | id, email (unique), password (BCrypt), first_name, last_name, role, enabled |
| categories | Product groups | id, name (unique), description |
| products | Catalog | id, name, description, price, stock, category_id, image_url |
| cart_items | Cart per user | id, user_id, product_id, quantity (unique per user+product) |
| orders | Orders | id, user_id, order_date, total_amount, status, shipping_address, payment_method |
| order_items | Order lines | id, order_id, product_id, quantity, price (price at purchase) |
| reviews | Ratings | id, user_id, product_id, rating, comment, created_at |

Relationships: user 1–N orders, cart items, reviews · category 1–N products · order 1–N order_items · product 1–N cart_items / order_items / reviews.

## 5. Features implemented
1. Registration (validated, duplicate email check) and login by email; roles USER and ADMIN.
2. Catalog with categories, keyword search, category and price filters, sorting and pagination.
3. Product detail with reviews, average rating and related products.
4. Database-backed cart with stock-aware quantity limits.
5. Checkout with shipping address and simulated Card / UPI / COD payment.
6. Order history, order detail with status tracker, customer cancellation.
7. Admin: dashboard statistics, product CRUD with image upload, category creation, order status updates, user enable/disable, low-stock list.
8. Optional email confirmation through Spring Mail.

## 6. Security and quality measures
- Passwords hashed with BCrypt; CSRF tokens on all forms; `/admin/**` restricted to ADMIN.
- Users can only access their own orders and cart rows.
- Upload validation (extension whitelist, size limit, random filenames).
- Bean Validation on all form input with inline error messages.
- Transactions protect stock and order consistency.
- Automated tests: order flow, search, and security rules.

## 7. Challenges and solutions
| Challenge | Solution |
|---|---|
| Dynamic search with optional filters | JPA `Specification` builds the query from only the filters provided |
| Keeping stock correct when ordering or cancelling | Single transaction for checkout; cancel restores stock |
| Old orders must not change when prices change | `order_items.price` stores the price at purchase |
| Deleting products referenced by orders | Blocked with a clear message; stock can be set to 0 |
| Testing without MySQL | H2 in MySQL mode for the test profile |

## 8. Learning outcomes
- Designing a layered Spring Boot application and mapping a relational schema with JPA.
- Configuring Spring Security with a database-backed `UserDetailsService`.
- Writing transactional business logic and validating input at the boundary.
- Building server-rendered, responsive pages with Thymeleaf.
- Writing integration tests for services and security rules.
- Packaging with Docker for deployment.

## 9. Future work
Real payment gateway (Razorpay/Stripe), wishlist, recommendations, REST API for a mobile app, email verification and password reset, and an audit log for admin actions.
