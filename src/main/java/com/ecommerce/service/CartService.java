package com.ecommerce.service;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Database-backed cart: one row per (user, product). */
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public List<CartItem> getItems(User user) { return cartItemRepository.findByUserOrderByIdAsc(user); }

    public long countUnits(User user) {
        Long n = cartItemRepository.sumUnits(user);
        return n == null ? 0 : n;
    }

    public BigDecimal total(List<CartItem> items) {
        return items.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void add(User user, Long productId, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be at least 1");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        CartItem item = cartItemRepository.findByUserAndProduct(user, product).orElseGet(() -> {
            CartItem c = new CartItem();
            c.setUser(user);
            c.setProduct(product);
            c.setQuantity(0);
            return c;
        });
        int newQty = item.getQuantity() + quantity;
        if (newQty > product.getStock()) {
            throw new IllegalArgumentException(product.getStock() == 0
                    ? product.getName() + " is out of stock"
                    : "Only " + product.getStock() + " of " + product.getName() + " available");
        }
        item.setQuantity(newQty);
        cartItemRepository.save(item);
    }

    @Transactional
    public void updateQuantity(User user, Long itemId, int quantity) {
        CartItem item = owned(user, itemId);
        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return;
        }
        if (quantity > item.getProduct().getStock()) {
            throw new IllegalArgumentException("Only " + item.getProduct().getStock() + " of "
                    + item.getProduct().getName() + " available");
        }
        item.setQuantity(quantity);
    }

    @Transactional
    public void remove(User user, Long itemId) { cartItemRepository.delete(owned(user, itemId)); }

    @Transactional
    public void clear(User user) { cartItemRepository.deleteByUser(user); }

    private CartItem owned(User user, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        if (!item.getUser().getId().equals(user.getId())) throw new IllegalArgumentException("Cart item not found");
        return item;
    }
}
