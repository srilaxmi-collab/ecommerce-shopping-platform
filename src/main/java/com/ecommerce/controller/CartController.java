package com.ecommerce.controller;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.User;
import com.ecommerce.service.CartService;
import com.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public String view(Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        List<CartItem> items = cartService.getItems(user);
        model.addAttribute("items", items);
        model.addAttribute("total", cartService.total(items));
        return "cart";
    }

    @PostMapping("/add")
    public String add(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity,
                      Principal principal, RedirectAttributes ra) {
        try {
            cartService.add(userService.getByEmail(principal.getName()), productId, quantity);
            ra.addFlashAttribute("success", "Added to your cart");
            return "redirect:/cart";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/products/" + productId;
        }
    }

    @PostMapping("/update")
    public String update(@RequestParam Long itemId, @RequestParam int quantity, Principal principal, RedirectAttributes ra) {
        try {
            cartService.updateQuantity(userService.getByEmail(principal.getName()), itemId, quantity);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam Long itemId, Principal principal, RedirectAttributes ra) {
        try {
            cartService.remove(userService.getByEmail(principal.getName()), itemId);
            ra.addFlashAttribute("success", "Item removed");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }
}
