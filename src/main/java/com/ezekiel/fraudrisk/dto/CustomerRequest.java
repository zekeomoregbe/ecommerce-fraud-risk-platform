package com.ezekiel.fraudrisk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;

public record CustomerRequest(
        @NotBlank @Email String email,
        @NotBlank String billingAddress,
        @NotBlank String shippingAddress,
        @PastOrPresent LocalDateTime accountCreatedAt
) {}
