package com.ecommerce.service;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    public static final int PAGE_SIZE = 9;
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;
    private final ReviewRepository reviewRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    /** Search by keyword (name or description), category and price range, with sorting and paging. */
    public Page<Product> search(String q, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                String sort, int page) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }
            if (categoryId != null) predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("price"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("price"), maxPrice));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return productRepository.findAll(spec, PageRequest.of(Math.max(page, 0), PAGE_SIZE, sortFor(sort)));
    }

    private Sort sortFor(String sort) {
        if (sort == null) return Sort.by(Sort.Direction.DESC, "id");
        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
    }

    public List<Product> latest() { return productRepository.findTop8ByOrderByIdDesc(); }
    public List<Product> lowStock() { return productRepository.findByStockLessThanEqualOrderByStockAsc(5); }
    public List<Product> related(Product p) {
        if (p.getCategory() == null) return List.of();
        return productRepository.findTop4ByCategoryIdAndIdNotOrderByIdDesc(p.getCategory().getId(), p.getId());
    }
    public Product get(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
    public List<Product> findAll() { return productRepository.findAll(Sort.by(Sort.Direction.DESC, "id")); }
    public long count() { return productRepository.count(); }

    public List<Category> categories() { return categoryRepository.findAll(Sort.by("name")); }

    @Transactional
    public void addCategory(String name, String description) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Enter a category name");
        if (categoryRepository.existsByNameIgnoreCase(name.trim())) throw new IllegalArgumentException("That category already exists");
        categoryRepository.save(new Category(name.trim(), description));
    }

    public ProductDto toDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setStock(p.getStock());
        dto.setCategoryId(p.getCategory() == null ? null : p.getCategory().getId());
        dto.setImageUrl(p.getImageUrl());
        return dto;
    }

    @Transactional
    public Product save(ProductDto dto, MultipartFile image) {
        Product p = dto.getId() == null ? new Product() : get(dto.getId());
        p.setName(dto.getName().trim());
        p.setDescription(dto.getDescription().trim());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());
        p.setCategory(categoryRepository.findById(dto.getCategoryId()).orElseThrow());
        if (image != null && !image.isEmpty()) {
            p.setImageUrl(storeImage(image));
        }
        return productRepository.save(p);
    }

    /** Deletes a product. Fails if it appears in past orders (order history must stay intact). */
    @Transactional
    public void delete(Long id) {
        Product p = get(id);
        cartItemRepository.deleteByProduct(p);
        reviewRepository.deleteByProduct(p);
        productRepository.delete(p);
        productRepository.flush();
    }

    private String storeImage(MultipartFile file) {
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        int dot = original.lastIndexOf('.');
        String ext = dot >= 0 ? original.substring(dot + 1).toLowerCase() : "";
        if (!IMAGE_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Upload a JPG, PNG, GIF or WEBP image");
        }
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "." + ext;   // random name: no path traversal, no collisions
            Files.copy(file.getInputStream(), dir.resolve(filename));
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the image", e);
        }
    }
}
