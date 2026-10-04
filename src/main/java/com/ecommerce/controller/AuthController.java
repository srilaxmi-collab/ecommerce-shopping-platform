package com.ecommerce.controller;

import com.ecommerce.dto.RegistrationDto;
import com.ecommerce.model.Order;
import com.ecommerce.model.User;
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
public class AuthController {
    private final UserService userService;
    private final OrderService orderService;

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new RegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") RegistrationDto dto, BindingResult result) {
        if (!result.hasFieldErrors("email") && userService.emailExists(dto.getEmail())) {
            result.rejectValue("email", "exists", "An account with this email already exists");
        }
        if (result.hasErrors()) return "register";
        userService.register(dto);
        return "redirect:/login?registered";
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        List<Order> orders = orderService.findForUser(user);
        model.addAttribute("profileUser", user);
        model.addAttribute("orderCount", orders.size());
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Principal principal, @RequestParam String firstName, @RequestParam String lastName,
                                RedirectAttributes ra) {
        if (firstName.isBlank() || lastName.isBlank()) {
            ra.addFlashAttribute("error", "Enter both first and last name");
        } else {
            userService.updateName(userService.getByEmail(principal.getName()), firstName, lastName);
            ra.addFlashAttribute("success", "Profile updated");
        }
        return "redirect:/profile";
    }
}
