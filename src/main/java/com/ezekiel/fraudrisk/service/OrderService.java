package com.ezekiel.fraudrisk.service;

import com.ezekiel.fraudrisk.dto.OrderRequest;
import com.ezekiel.fraudrisk.dto.OrderResponse;
import com.ezekiel.fraudrisk.dto.OrderUpdateRequest;
import com.ezekiel.fraudrisk.dto.RiskScoreResult;
import com.ezekiel.fraudrisk.entity.Customer;
import com.ezekiel.fraudrisk.entity.CustomerOrder;
import com.ezekiel.fraudrisk.exception.ResourceNotFoundException;
import com.ezekiel.fraudrisk.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final PythonRiskEngineClient riskEngine;

    public OrderService(OrderRepository orderRepository, CustomerService customerService, PythonRiskEngineClient riskEngine) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.riskEngine = riskEngine;
    }

    public OrderResponse create(OrderRequest request) {
        Customer customer = customerService.require(request.customerId());
        return saveScoredOrder(new CustomerOrder(), customer, request.orderValue(), request.billingAddress(), request.shippingAddress());
    }

    public List<OrderResponse> list() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    public OrderResponse get(Long id) {
        return toResponse(require(id));
    }

    public List<OrderResponse> listByCustomer(Long customerId) {
        customerService.require(customerId);
        return orderRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId).stream().map(this::toResponse).toList();
    }

    public OrderResponse update(Long id, OrderUpdateRequest request) {
        CustomerOrder order = require(id);
        Customer customer = order.getCustomer();
        return saveScoredOrder(order, customer, request.orderValue(), request.billingAddress(), request.shippingAddress());
    }

    public void delete(Long id) {
        orderRepository.delete(require(id));
    }

    private OrderResponse saveScoredOrder(CustomerOrder order, Customer customer, BigDecimal orderValue,
                                          String submittedBillingAddress, String submittedShippingAddress) {
        String billing = normalizeOrDefault(submittedBillingAddress, customer.getBillingAddress());
        String shipping = normalizeOrDefault(submittedShippingAddress, customer.getShippingAddress());
        boolean mismatch = !normalize(billing).equals(normalize(shipping));

        long accountAgeDays = Math.max(0, Duration.between(customer.getAccountCreatedAt(), LocalDateTime.now()).toDays());
        long recentOrders = orderRepository.countByCustomer_IdAndCreatedAtAfter(customer.getId(), LocalDateTime.now().minusHours(24));
        if (order.getId() != null && order.getCreatedAt() != null && order.getCreatedAt().isAfter(LocalDateTime.now().minusHours(24))) {
            recentOrders = Math.max(0, recentOrders - 1);
        }

        RiskScoreResult result = riskEngine.score(accountAgeDays, orderValue, recentOrders, mismatch);

        order.setCustomer(customer);
        order.setOrderValue(orderValue);
        order.setAddressMismatch(mismatch);
        order.setRecentOrderCount((int) recentOrders);
        order.setRiskScore(result.score());
        order.setRiskLevel(result.level());
        order.setRiskReasons(result.reasons());

        return toResponse(orderRepository.save(order));
    }

    private CustomerOrder require(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + id + " was not found"));
    }

    private String normalizeOrDefault(String supplied, String fallback) {
        return supplied == null || supplied.isBlank() ? fallback : supplied.trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim().toLowerCase();
    }

    private OrderResponse toResponse(CustomerOrder order) {
        return new OrderResponse(
                order.getId(), order.getCustomer().getId(), order.getOrderValue(), order.getCreatedAt(),
                order.getRiskScore(), order.getRiskLevel(), order.getRiskReasons(),
                order.getAddressMismatch(), order.getRecentOrderCount()
        );
    }
}
