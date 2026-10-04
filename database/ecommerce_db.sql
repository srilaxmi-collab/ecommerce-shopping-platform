-- ShopEase / Online Shopping Platform - MySQL dump (schema + sample data)
-- Import:  mysql -u root -p < ecommerce_db.sql
-- Logins:  admin@shopease.com / Admin@123   |   user@shopease.com / User@123
-- The app also creates this schema automatically (spring.jpa.hibernate.ddl-auto=update).

CREATE DATABASE IF NOT EXISTS ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS reviews, order_items, orders, cart_items, products, categories, users;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  role VARCHAR(20) NOT NULL,
  enabled BIT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE categories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  PRIMARY KEY (id),
  UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB;

CREATE TABLE products (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(2000),
  price DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL,
  image_url VARCHAR(255),
  category_id BIGINT,
  PRIMARY KEY (id),
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB;

CREATE TABLE cart_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cart_user_product (user_id, product_id),
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB;

CREATE TABLE orders (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  order_date DATETIME(6) NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  shipping_address VARCHAR(500) NOT NULL,
  payment_method VARCHAR(255),
  PRIMARY KEY (id),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE order_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders (id),
  CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB;

CREATE TABLE reviews (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  rating INT NOT NULL,
  comment VARCHAR(1000),
  created_at DATETIME(6),
  PRIMARY KEY (id),
  CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB;

-- ---------------- sample data ----------------
INSERT INTO users (id, email, password, first_name, last_name, role, enabled) VALUES
 (1, 'admin@shopease.com', '$2a$10$HDLU7BdK52O.rEzL6nuIdeglCkjXNfXy1fqmTIBD8goU96dSSrs3G', 'Asha', 'Admin', 'ADMIN', 1),
 (2, 'user@shopease.com',  '$2a$10$eN6eWLUpcdhUURN9GuKWSuB857k1zEP.Q70NwaDoqntg.8dd5BaFC',  'Ravi', 'Kumar', 'USER', 1);

INSERT INTO categories (id, name, description) VALUES
 (1, 'Electronics', 'Phones, audio and accessories'),
 (2, 'Books', 'Programming and fiction'),
 (3, 'Fashion', 'Everyday clothing and bags'),
 (4, 'Home & Kitchen', 'Cookware and home essentials');

INSERT INTO products (id, name, description, price, stock, image_url, category_id) VALUES
 (1, 'Wireless Earbuds', 'Bluetooth 5.3 earbuds with 24-hour battery life and a charging case.', 2499.00, 38, NULL, 1),
 (2, 'Smart Watch', 'Fitness tracking, heart-rate monitor and 7-day battery.', 3999.00, 25, NULL, 1),
 (3, '20000 mAh Power Bank', 'Fast-charging power bank with two USB ports.', 1799.00, 60, NULL, 1),
 (4, 'Mechanical Keyboard', 'Compact 75% layout with hot-swappable switches.', 3299.00, 15, NULL, 1),
 (5, 'Effective Java (3rd Edition)', 'Joshua Bloch''s guide to writing clear, robust Java.', 899.00, 29, NULL, 2),
 (6, 'Clean Code', 'A handbook of agile software craftsmanship by Robert C. Martin.', 749.00, 35, NULL, 2),
 (7, 'Spring in Action', 'Build Spring applications from first controller to production.', 1099.00, 20, NULL, 2),
 (8, 'Cotton Crew T-Shirt', 'Soft 100% cotton tee in a relaxed fit.', 499.00, 100, NULL, 3),
 (9, 'Canvas Backpack', 'Water-resistant 25 L backpack with a padded laptop sleeve.', 1299.00, 45, NULL, 3),
 (10, 'Non-Stick Frying Pan', '28 cm induction-compatible pan with a cool-touch handle.', 999.00, 50, NULL, 4),
 (11, 'Stainless Steel Bottle', '1 litre insulated bottle that keeps drinks cold for 24 hours.', 649.00, 3, NULL, 4),
 (12, 'LED Desk Lamp', 'Dimmable desk lamp with three colour temperatures.', 1199.00, 28, NULL, 4);

INSERT INTO orders (id, user_id, order_date, total_amount, status, shipping_address, payment_method) VALUES
 (1, 2, '2026-09-20 10:15:00', 3398.00, 'DELIVERED', 'Ravi Kumar, 12 MG Road, Warangal, Telangana 506002', 'UPI'),
 (2, 2, '2026-10-01 18:40:00', 4998.00, 'PAID', 'Ravi Kumar, 12 MG Road, Warangal, Telangana 506002', 'CARD');

INSERT INTO order_items (id, order_id, product_id, quantity, price) VALUES
 (1, 1, 5, 1, 899.00),
 (2, 1, 1, 1, 2499.00),
 (3, 2, 1, 2, 2499.00);

INSERT INTO reviews (id, user_id, product_id, rating, comment, created_at) VALUES
 (1, 2, 5, 5, 'Essential reading for any Java developer.', '2026-09-25 09:00:00'),
 (2, 2, 1, 4, 'Great sound and battery life for the price.', '2026-09-26 20:30:00');
