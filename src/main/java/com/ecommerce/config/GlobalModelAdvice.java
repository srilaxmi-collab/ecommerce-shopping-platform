package com.ecommerce.config;

import com.ecommerce.service.CartService;
import com.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/** Adds the cart badge count to every page. */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {
    private final UserService userService;
    private final CartService cartService;

    @ModelAttribute("cartCount")
    public long cartCount(Principal principal) {
        if (principal == null) return 0;
        return userService.findByEmail(principal.getName()).map(cartService::countUnits).orElse(0L);
    }
}
