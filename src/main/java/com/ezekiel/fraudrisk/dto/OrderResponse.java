package com.ezekiel.fraudrisk.dto;

import com.ezekiel.fraudrisk.entity.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        BigDecimal orderValue,
        LocalDateTime createdAt,
        Integer riskScore,
        RiskLevel riskLevel,
        List<String> riskReasons,
        Boolean addressMismatch,
        Integer recentOrderCount
) {}
