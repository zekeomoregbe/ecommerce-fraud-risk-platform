package com.ezekiel.fraudrisk.repository;

import com.ezekiel.fraudrisk.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);
    long countByCustomer_IdAndCreatedAtAfter(Long customerId, LocalDateTime after);
}
