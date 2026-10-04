package com.ecommerce.controller;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** All /admin/** routes are restricted to ROLE_ADMIN in SecurityConfig. */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("productCount", productService.count());
        model.addAttribute("orderCount", orderService.count());
        model.addAttribute("userCount", userService.findAll().size());
        model.addAttribute("revenue", orderService.revenue());
        model.addAttribute("lowStock", productService.lowStock());
        model.addAttribute("recentOrders", orderService.findAll().stream().limit(5).toList());
        return "admin/dashboard";
    }

    // ---- Products ----
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("categories", productService.categories());
        return "admin/product-list";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new ProductDto());
        model.addAttribute("categories", productService.categories());
        return "admin/product-form";
    }

    @GetMapping("/products/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.toDto(productService.get(id)));
        model.addAttribute("categories", productService.categories());
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String save(@Valid @ModelAttribute("product") ProductDto dto, BindingResult result,
                       @RequestParam(value = "image", required = false) MultipartFile image,
                       Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                productService.save(dto, image);
                ra.addFlashAttribute("success", "Product saved");
                return "redirect:/admin/products";
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("categories", productService.categories());
        return "admin/product-form";
    }

    @PostMapping("/products/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.delete(id);
            ra.addFlashAttribute("success", "Product deleted");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("error", "This product is in past orders and can't be deleted. Set its stock to 0 instead.");
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/categories")
    public String addCategory(@RequestParam String name, @RequestParam(required = false) String description,
                              RedirectAttributes ra) {
        try {
            productService.addCategory(name, description);
            ra.addFlashAttribute("success", "Category added");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // ---- Orders ----
    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderService.findAll());
        model.addAttribute("statuses", List.of(OrderStatus.values()));
        return "admin/order-list";
    }

    @PostMapping("/orders/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus status, RedirectAttributes ra) {
        try {
            orderService.updateStatus(id, status);
            ra.addFlashAttribute("success", "Order #" + id + " is now " + status);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    // ---- Users ----
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/user-list";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, java.security.Principal principal, RedirectAttributes ra) {
        if (userService.getByEmail(principal.getName()).getId().equals(id)) {
            ra.addFlashAttribute("error", "You can't disable your own account");
        } else {
            userService.toggleEnabled(id);
            ra.addFlashAttribute("success", "User updated");
        }
        return "redirect:/admin/users";
    }
}
