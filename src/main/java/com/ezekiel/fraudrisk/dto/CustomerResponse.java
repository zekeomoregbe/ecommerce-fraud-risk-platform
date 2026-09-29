package com.ezekiel.fraudrisk.dto;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String email,
        String billingAddress,
        String shippingAddress,
        LocalDateTime accountCreatedAt,
        LocalDateTime createdAt
) {}
