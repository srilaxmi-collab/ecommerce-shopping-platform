package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.model.Review;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ReviewRepository reviewRepository;
    private final UserService userService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", productService.latest());
        model.addAttribute("categories", productService.categories());
        return "index";
    }

    @GetMapping("/products")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Long category,
                       @RequestParam(required = false) BigDecimal minPrice,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       @RequestParam(defaultValue = "newest") String sort,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("page", productService.search(q, category, minPrice, maxPrice, sort, page));
        model.addAttribute("categories", productService.categories());
        model.addAttribute("q", q);
        model.addAttribute("category", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        return "products";
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Product product = productService.get(id);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewRepository.findByProductOrderByCreatedAtDesc(product));
        Double avg = reviewRepository.averageRating(product);
        model.addAttribute("avgRating", avg == null ? null : Math.round(avg * 10) / 10.0);
        model.addAttribute("related", productService.related(product));
        return "product-detail";
    }

    @PostMapping("/products/{id}/reviews")
    public String review(@PathVariable Long id, @RequestParam int rating, @RequestParam(required = false) String comment,
                         Principal principal, RedirectAttributes ra) {
        if (rating < 1 || rating > 5) {
            ra.addFlashAttribute("error", "Choose a rating from 1 to 5");
        } else {
            Review r = new Review();
            r.setUser(userService.getByEmail(principal.getName()));
            r.setProduct(productService.get(id));
            r.setRating(rating);
            r.setComment(comment == null ? null : comment.trim());
            reviewRepository.save(r);
            ra.addFlashAttribute("success", "Thanks, your review is posted");
        }
        return "redirect:/products/" + id;
    }
}
