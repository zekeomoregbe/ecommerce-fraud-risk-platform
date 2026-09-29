package com.ezekiel.fraudrisk.service;

import com.ezekiel.fraudrisk.dto.OrderRequest;
import com.ezekiel.fraudrisk.dto.RiskScoreResult;
import com.ezekiel.fraudrisk.entity.Customer;
import com.ezekiel.fraudrisk.entity.CustomerOrder;
import com.ezekiel.fraudrisk.entity.RiskLevel;
import com.ezekiel.fraudrisk.repository.OrderRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Test
    void createStoresRiskResult() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        CustomerService customerService = mock(CustomerService.class);
        PythonRiskEngineClient riskEngine = mock(PythonRiskEngineClient.class);

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmail("demo@example.com");
        customer.setBillingAddress("100 Main St");
        customer.setShippingAddress("100 Main St");
        customer.setAccountCreatedAt(LocalDateTime.now().minusDays(5));

        when(customerService.require(1L)).thenReturn(customer);
        when(orderRepository.countByCustomer_IdAndCreatedAtAfter(anyLong(), any())).thenReturn(0L);
        when(riskEngine.score(anyLong(), any(BigDecimal.class), anyLong(), anyBoolean()))
                .thenReturn(new RiskScoreResult(40, RiskLevel.MEDIUM, List.of("New account")));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> {
            CustomerOrder order = invocation.getArgument(0);
            order.setId(99L);
            order.setCreatedAt(LocalDateTime.now());
            return order;
        });

        OrderService service = new OrderService(orderRepository, customerService, riskEngine);
        var response = service.create(new OrderRequest(1L, new BigDecimal("250.00"), null, null));

        assertEquals(99L, response.id());
        assertEquals(40, response.riskScore());
        assertEquals(RiskLevel.MEDIUM, response.riskLevel());
        verify(orderRepository).save(any(CustomerOrder.class));
    }
}
