package com.ecommerce.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ProductDto {
    private Long id;

    @NotBlank(message = "Enter a product name")
    private String name;

    @NotBlank(message = "Enter a description")
    private String description;

    @NotNull(message = "Enter a price")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    @NotNull(message = "Choose a category")
    private Long categoryId;

    /** Existing image path, kept when no new file is uploaded. */
    private String imageUrl;
}
