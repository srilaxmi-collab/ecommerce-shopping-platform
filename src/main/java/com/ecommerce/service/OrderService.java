package com.ecommerce.service;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.model.*;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentService paymentService;
    private final EmailService emailService;

    /**
     * Converts the user's cart into an order in one transaction:
     * validates stock, decrements it, snapshots prices, takes (simulated) payment and empties the cart.
     */
    @Transactional
    public Order placeOrder(User user, OrderDto dto) {
        List<CartItem> cart = cartItemRepository.findByUserOrderByIdAsc(user);
        if (cart.isEmpty()) throw new IllegalStateException("Your cart is empty");

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(dto.getShippingAddress().trim());
        order.setPaymentMethod(dto.getPaymentMethod());

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart) {
            Product p = ci.getProduct();
            if (p.getStock() < ci.getQuantity()) {
                throw new IllegalStateException("Only " + p.getStock() + " of " + p.getName() + " left. Update your cart.");
            }
            p.setStock(p.getStock() - ci.getQuantity());

            OrderItem oi = new OrderItem();
            oi.setProduct(p);
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(p.getPrice());
            order.addItem(oi);
            total = total.add(oi.getSubtotal());
        }
        order.setTotalAmount(total);
        order.setStatus(paymentService.charge(dto.getPaymentMethod(), total) ? OrderStatus.PAID : OrderStatus.PENDING);

        Order saved = orderRepository.save(order);
        cartItemRepository.deleteByUser(user);
        emailService.sendOrderConfirmation(saved);
        return saved;
    }

    public List<Order> findForUser(User user) { return orderRepository.findByUserOrderByOrderDateDesc(user); }
    public List<Order> findAll() { return orderRepository.findAllByOrderByOrderDateDesc(); }
    public long count() { return orderRepository.count(); }
    public BigDecimal revenue() {
        BigDecimal r = orderRepository.totalRevenue();
        return r == null ? BigDecimal.ZERO : r;
    }

    /** Returns the order only if it belongs to the user (404 otherwise, so ids can't be probed). */
    public Order getForUser(User user, Long id) {
        Order o = orderRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!o.getUser().getId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return o;
    }

    @Transactional
    public void cancel(User user, Long id) {
        Order o = getForUser(user, id);
        if (o.getStatus() != OrderStatus.PENDING && o.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("This order can no longer be cancelled");
        }
        restoreStock(o);
        o.setStatus(OrderStatus.CANCELLED);
    }

    /** Admin: change status. Cancelling returns stock to inventory. */
    @Transactional
    public void updateStatus(Long id, OrderStatus status) {
        Order o = orderRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (o.getStatus() == OrderStatus.CANCELLED) throw new IllegalStateException("Cancelled orders cannot be changed");
        if (status == OrderStatus.CANCELLED) restoreStock(o);
        o.setStatus(status);
    }

    private void restoreStock(Order o) {
        for (OrderItem oi : o.getItems()) {
            oi.getProduct().setStock(oi.getProduct().getStock() + oi.getQuantity());
        }
    }
}
