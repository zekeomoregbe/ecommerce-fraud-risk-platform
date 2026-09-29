package com.ezekiel.fraudrisk.dto;

import com.ezekiel.fraudrisk.entity.RiskLevel;
import java.util.List;

public record RiskScoreResult(
        int score,
        RiskLevel level,
        List<String> reasons
) {}
