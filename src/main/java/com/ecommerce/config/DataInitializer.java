package com.ecommerce.config;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Seeds default accounts and sample catalog the first time the app starts. */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (users.count() == 0) {
            users.save(user("admin@shopease.com", "Admin@123", "Asha", "Admin", Role.ADMIN));
            users.save(user("user@shopease.com", "User@123", "Ravi", "Kumar", Role.USER));
        }
        if (categories.count() == 0) {
            Category electronics = categories.save(new Category("Electronics", "Phones, audio and accessories"));
            Category books = categories.save(new Category("Books", "Programming and fiction"));
            Category fashion = categories.save(new Category("Fashion", "Everyday clothing and bags"));
            Category home = categories.save(new Category("Home & Kitchen", "Cookware and home essentials"));

            product("Wireless Earbuds", "Bluetooth 5.3 earbuds with 24-hour battery life and a charging case.", "2499.00", 40, electronics);
            product("Smart Watch", "Fitness tracking, heart-rate monitor and 7-day battery.", "3999.00", 25, electronics);
            product("20000 mAh Power Bank", "Fast-charging power bank with two USB ports.", "1799.00", 60, electronics);
            product("Mechanical Keyboard", "Compact 75% layout with hot-swappable switches.", "3299.00", 15, electronics);
            product("Effective Java (3rd Edition)", "Joshua Bloch's guide to writing clear, robust Java.", "899.00", 30, books);
            product("Clean Code", "A handbook of agile software craftsmanship by Robert C. Martin.", "749.00", 35, books);
            product("Spring in Action", "Build Spring applications from first controller to production.", "1099.00", 20, books);
            product("Cotton Crew T-Shirt", "Soft 100% cotton tee in a relaxed fit.", "499.00", 100, fashion);
            product("Canvas Backpack", "Water-resistant 25 L backpack with a padded laptop sleeve.", "1299.00", 45, fashion);
            product("Non-Stick Frying Pan", "28 cm induction-compatible pan with a cool-touch handle.", "999.00", 50, home);
            product("Stainless Steel Bottle", "1 litre insulated bottle that keeps drinks cold for 24 hours.", "649.00", 3, home);
            product("LED Desk Lamp", "Dimmable desk lamp with three colour temperatures.", "1199.00", 28, home);
        }
    }

    private User user(String email, String rawPassword, String first, String last, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setFirstName(first);
        u.setLastName(last);
        u.setRole(role);
        return u;
    }

    private void product(String name, String description, String price, int stock, Category category) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setCategory(category);
        products.save(p);
    }
}
