# ShopEase – Online Shopping Platform

Task ID **JV-EC-001** · Data Alcott Systems · Free Java Full Stack Internship  
Task link: https://www.freeinternships.in/blog/

A full-stack e-commerce web app: browse and search products, keep a cart, check out with simulated payment, track orders, and manage the store from an admin dashboard.

**Stack:** Java 17 · Spring Boot 3.3 · Spring MVC · Spring Security · Spring Data JPA / Hibernate · MySQL 8 · Thymeleaf · Maven

## Features

| Area | What's included |
|---|---|
| Accounts | Registration with validation, login by email, BCrypt passwords, roles `USER` / `ADMIN`, profile page |
| Catalog | Categories, keyword search (name + description), category and price filters, sorting, pagination, product detail |
| Cart | Database-backed cart, add / change quantity / remove, stock-aware limits, live total and navbar badge |
| Orders | Checkout with shipping address, simulated payment (Card / UPI / COD), stock decrement in one transaction, order history, status tracker, customer cancellation (restores stock) |
| Admin | Dashboard (counts, revenue, low-stock, recent orders), product CRUD with **image upload**, category creation, order status management, user enable / disable |
| Bonus | Product reviews and ratings, inventory tracking, order status tracking, product image upload, related products, optional order emails (Spring Mail) |

Not built: wishlist, recommendations, mobile REST API (the schema and services leave room for them).

## Quick start

**Prerequisites:** JDK 17+, Maven 3.9+, MySQL 8 (or Docker).

### Option A – local MySQL
1. Start MySQL. The database is created automatically (`createDatabaseIfNotExist=true`).
2. Set your credentials (or edit `application.properties`):
   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=your_password
   ```
3. Run:
   ```bash
   mvn spring-boot:run
   ```
4. Open http://localhost:8080

On first start the app seeds sample categories, 12 products and two accounts.

### Option B – Docker (app + MySQL)
```bash
docker compose up --build
```

### Option C – import the SQL dump instead
```bash
mysql -u root -p < database/ecommerce_db.sql
```
This creates the schema and loads sample data (2 users, 4 categories, 12 products, 2 orders, 2 reviews). Then run the app as in Option A.

## Demo accounts

| Role | Email | Password |
|---|---|---|
| Admin | `admin@shopease.com` | `Admin@123` |
| Customer | `user@shopease.com` | `User@123` |

Change these before any real deployment.

## Run the tests
```bash
mvn test
```
Tests use an in-memory H2 database, so no MySQL is needed. They cover the order flow (stock, pricing, cancel, COD), product search and filters, and security rules (public pages, login required, admin-only area, registration validation, login success/failure).

## Project structure
```
src/main/java/com/ecommerce
├── EcommerceApplication.java
├── config/       SecurityConfig, WebConfig (uploads), GlobalModelAdvice (cart badge), DataInitializer (seed)
├── controller/   AuthController, ProductController, CartController, OrderController, AdminController
├── model/        User, Role, Category, Product, CartItem, Order, OrderItem, OrderStatus, Review
├── repository/   Spring Data JPA repositories
├── service/      UserService (UserDetailsService), ProductService, CartService, OrderService, PaymentService, EmailService
└── dto/          RegistrationDto, ProductDto, OrderDto
src/main/resources/templates   Thymeleaf pages (+ admin/)
src/main/resources/static      css, js
database/ecommerce_db.sql      schema + sample data
docs/PROJECT_REPORT.md         2–3 page report
```

## Configuration

| Setting | Where | Default |
|---|---|---|
| DB URL / user / password | env `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `localhost:3306/ecommerce_db`, `root` |
| Upload folder | env `UPLOAD_DIR` | `./uploads` |
| Seed data on start | `app.seed.enabled` | `true` |
| Order emails | `app.mail.enabled` + `spring.mail.*` | off |

## Design notes
- **Layers:** controller → service → repository; controllers never touch repositories for writes, and all multi-step writes (checkout, cancel, delete product) are `@Transactional`.
- **Security:** CSRF protection on every form, BCrypt hashing, `/admin/**` requires `ROLE_ADMIN`, users can only view their own orders and cart rows (other ids return 404).
- **Uploads:** only JPG/PNG/GIF/WEBP, 5 MB limit, stored under a random UUID filename.
- **Order history stays intact:** order lines copy the price at purchase time, and products that appear in past orders can't be deleted (set stock to 0 instead).
- **Payments are simulated** in `PaymentService`. To go live, replace `charge()` with Razorpay/Stripe and verify the gateway signature server-side.

## Deployment
Any platform that runs a Java 17 JAR or a Docker image works (Render, Railway, AWS Elastic Beanstalk / ECS, Azure App Service). Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` and `UPLOAD_DIR` (use a persistent volume), and change the demo passwords.

## Submission checklist
- [ ] Public GitHub repo with this code and commits per day
- [ ] `database/ecommerce_db.sql` included
- [ ] Screenshots: Home, Products, Cart, Checkout, Admin Dashboard (desktop + mobile)
- [ ] `docs/PROJECT_REPORT.md` exported to a 2–3 page PDF
- [ ] 5–7 min demo video uploaded to YouTube (public/unlisted)
- [ ] Live demo URL
- [ ] Blog post at freeinternships.in with title `E-Commerce Shopping Platform - DAS-JV-001`
