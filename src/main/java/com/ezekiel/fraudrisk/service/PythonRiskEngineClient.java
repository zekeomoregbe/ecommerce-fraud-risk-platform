package com.ezekiel.fraudrisk.service;

import com.ezekiel.fraudrisk.dto.RiskScoreResult;
import com.ezekiel.fraudrisk.entity.RiskLevel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class PythonRiskEngineClient {
    private final ObjectMapper objectMapper;
    private final String pythonExecutable;
    private final String scriptPath;

    public PythonRiskEngineClient(
            ObjectMapper objectMapper,
            @Value("${risk.engine.python:python3}") String pythonExecutable,
            @Value("${risk.engine.script:scripts/risk_engine.py}") String scriptPath) {
        this.objectMapper = objectMapper;
        this.pythonExecutable = pythonExecutable;
        this.scriptPath = scriptPath;
    }

    public RiskScoreResult score(long accountAgeDays, BigDecimal orderValue, long orderFrequency, boolean addressMismatch) {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    pythonExecutable,
                    Path.of(scriptPath).toAbsolutePath().toString(),
                    "--account-age-days", Long.toString(accountAgeDays),
                    "--order-value", orderValue.toPlainString(),
                    "--order-frequency", Long.toString(orderFrequency),
                    "--address-mismatch", Boolean.toString(addressMismatch)
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) output.append(line);
            }

            int exit = process.waitFor();
            if (exit != 0) throw new IllegalStateException("Python risk engine failed: " + output);

            JsonNode json = objectMapper.readTree(output.toString());
            List<String> reasons = new ArrayList<>();
            json.path("reasons").forEach(node -> reasons.add(node.asText()));

            return new RiskScoreResult(
                    json.path("score").asInt(),
                    RiskLevel.valueOf(json.path("level").asText()),
                    reasons
            );
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate fraud risk", e);
        }
    }
}
