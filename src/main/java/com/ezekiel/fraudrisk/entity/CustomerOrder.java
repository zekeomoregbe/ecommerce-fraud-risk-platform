package com.ezekiel.fraudrisk.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal orderValue;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_risk_reasons", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "reason", nullable = false, length = 255)
    private List<String> riskReasons = new ArrayList<>();

    @Column(nullable = false)
    private Boolean addressMismatch;

    @Column(nullable = false)
    private Integer recentOrderCount;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public BigDecimal getOrderValue() { return orderValue; }
    public void setOrderValue(BigDecimal orderValue) { this.orderValue = orderValue; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public List<String> getRiskReasons() { return riskReasons; }
    public void setRiskReasons(List<String> riskReasons) { this.riskReasons = riskReasons; }
    public Boolean getAddressMismatch() { return addressMismatch; }
    public void setAddressMismatch(Boolean addressMismatch) { this.addressMismatch = addressMismatch; }
    public Integer getRecentOrderCount() { return recentOrderCount; }
    public void setRecentOrderCount(Integer recentOrderCount) { this.recentOrderCount = recentOrderCount; }
}
