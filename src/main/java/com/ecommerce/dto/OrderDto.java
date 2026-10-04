package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderDto {
    @NotBlank(message = "Enter a shipping address")
    @Size(max = 500, message = "Address is too long (max 500 characters)")
    private String shippingAddress;

    /** CARD, UPI or COD. Payment is simulated. */
    @NotBlank(message = "Choose a payment method")
    private String paymentMethod = "CARD";
}
