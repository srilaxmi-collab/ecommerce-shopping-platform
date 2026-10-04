package com.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegistrationDto {
    @NotBlank(message = "Enter your first name")
    private String firstName;

    @NotBlank(message = "Enter your last name")
    private String lastName;

    @NotBlank(message = "Enter your email")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Choose a password")
    @Size(min = 6, message = "Use at least 6 characters")
    private String password;
}
