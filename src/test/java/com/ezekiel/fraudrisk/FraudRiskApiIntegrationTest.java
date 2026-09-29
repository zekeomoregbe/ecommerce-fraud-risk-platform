package com.ezekiel.fraudrisk;

import com.ezekiel.fraudrisk.repository.CustomerRepository;
import com.ezekiel.fraudrisk.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FraudRiskApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void resetDatabase() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void createsCustomerAndScoresHighRiskOrderThroughPythonEngine() throws Exception {
        String customerJson = """
                {
                  "email": "integration@example.com",
                  "billingAddress": "100 Main St, Atlanta, GA",
                  "shippingAddress": "100 Main St, Atlanta, GA",
                  "accountCreatedAt": "2026-09-27T10:00:00"
                }
                """;

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("integration@example.com"));

        Long customerId = customerRepository.findByEmailIgnoreCase("integration@example.com")
                .orElseThrow()
                .getId();

        String orderJson = """
                {
                  "customerId": %d,
                  "orderValue": 725.00,
                  "billingAddress": "100 Main St, Atlanta, GA",
                  "shippingAddress": "900 Different Ave, Atlanta, GA"
                }
                """.formatted(customerId);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.riskScore").isNumber())
                .andExpect(jsonPath("$.addressMismatch").value(true))
                .andExpect(jsonPath("$.riskReasons").isArray());
    }
}
