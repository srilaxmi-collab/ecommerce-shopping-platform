package com.ecommerce.controller;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.User;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;

    @GetMapping("/checkout")
    public String checkout(Principal principal, Model model, RedirectAttributes ra) {
        User user = userService.getByEmail(principal.getName());
        List<CartItem> items = cartService.getItems(user);
        if (items.isEmpty()) {
            ra.addFlashAttribute("error", "Your cart is empty");
            return "redirect:/cart";
        }
        model.addAttribute("items", items);
        model.addAttribute("total", cartService.total(items));
        if (!model.containsAttribute("orderDto")) model.addAttribute("orderDto", new OrderDto());
        return "checkout";
    }

    @PostMapping("/checkout")
    public String place(@Valid @ModelAttribute("orderDto") OrderDto dto, BindingResult result,
                        Principal principal, Model model, RedirectAttributes ra) {
        User user = userService.getByEmail(principal.getName());
        if (!result.hasErrors()) {
            try {
                Order order = orderService.placeOrder(user, dto);
                ra.addFlashAttribute("success", "Order placed. Thank you for shopping with us!");
                return "redirect:/orders/" + order.getId();
            } catch (IllegalStateException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        List<CartItem> items = cartService.getItems(user);
        model.addAttribute("items", items);
        model.addAttribute("total", cartService.total(items));
        return "checkout";
    }

    @GetMapping("/orders")
    public String history(Principal principal, Model model) {
        model.addAttribute("orders", orderService.findForUser(userService.getByEmail(principal.getName())));
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("order", orderService.getForUser(userService.getByEmail(principal.getName()), id));
        return "order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        try {
            orderService.cancel(userService.getByEmail(principal.getName()), id);
            ra.addFlashAttribute("success", "Order cancelled");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}
