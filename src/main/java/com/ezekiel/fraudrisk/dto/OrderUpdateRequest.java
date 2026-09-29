package com.ezekiel.fraudrisk.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderUpdateRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal orderValue,
        String billingAddress,
        String shippingAddress
) {}
