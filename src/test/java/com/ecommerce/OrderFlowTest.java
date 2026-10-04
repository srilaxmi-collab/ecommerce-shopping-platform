package com.ecommerce;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.dto.RegistrationDto;
import com.ecommerce.model.*;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** End-to-end service tests against an in-memory H2 database. Each test rolls back. */
@SpringBootTest
@Transactional
class OrderFlowTest {
    @Autowired UserService userService;
    @Autowired CartService cartService;
    @Autowired OrderService orderService;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;

    User user;
    Product lamp;

    @BeforeEach
    void setUp() {
        RegistrationDto dto = new RegistrationDto();
        dto.setFirstName("Test");
        dto.setLastName("Buyer");
        dto.setEmail(UUID.randomUUID() + "@example.com");
        dto.setPassword("secret1");
        user = userService.register(dto);

        Category c = categoryRepository.save(new Category("Test-" + UUID.randomUUID(), "test"));
        lamp = new Product();
        lamp.setName("Test Lamp");
        lamp.setDescription("A lamp");
        lamp.setPrice(new BigDecimal("100.00"));
        lamp.setStock(5);
        lamp.setCategory(c);
        lamp = productRepository.save(lamp);
    }

    private OrderDto checkout(String method) {
        OrderDto dto = new OrderDto();
        dto.setShippingAddress("1 Test Street, Hyderabad");
        dto.setPaymentMethod(method);
        return dto;
    }

    @Test
    void passwordIsStoredHashed() {
        assertNotEquals("secret1", user.getPassword());
        assertTrue(user.getPassword().startsWith("$2"));
    }

    @Test
    void placingOrderDecrementsStockClearsCartAndSnapshotsPrice() {
        cartService.add(user, lamp.getId(), 2);
        Order order = orderService.placeOrder(user, checkout("CARD"));

        assertEquals(0, new BigDecimal("200.00").compareTo(order.getTotalAmount()));
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(1, order.getItems().size());
        assertEquals(3, productRepository.findById(lamp.getId()).orElseThrow().getStock());
        assertTrue(cartService.getItems(user).isEmpty());
    }

    @Test
    void cashOnDeliveryStaysPending() {
        cartService.add(user, lamp.getId(), 1);
        assertEquals(OrderStatus.PENDING, orderService.placeOrder(user, checkout("COD")).getStatus());
    }

    @Test
    void cannotAddMoreThanStock() {
        assertThrows(IllegalArgumentException.class, () -> cartService.add(user, lamp.getId(), 6));
    }

    @Test
    void cannotCheckoutWithEmptyCart() {
        assertThrows(IllegalStateException.class, () -> orderService.placeOrder(user, checkout("CARD")));
    }

    @Test
    void cancellingRestoresStock() {
        cartService.add(user, lamp.getId(), 2);
        Order order = orderService.placeOrder(user, checkout("CARD"));
        orderService.cancel(user, order.getId());

        assertEquals(OrderStatus.CANCELLED, orderService.getForUser(user, order.getId()).getStatus());
        assertEquals(5, productRepository.findById(lamp.getId()).orElseThrow().getStock());
    }
}
