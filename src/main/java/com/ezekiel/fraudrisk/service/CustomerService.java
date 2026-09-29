package com.ezekiel.fraudrisk.service;

import com.ezekiel.fraudrisk.dto.CustomerRequest;
import com.ezekiel.fraudrisk.dto.CustomerResponse;
import com.ezekiel.fraudrisk.entity.Customer;
import com.ezekiel.fraudrisk.exception.ConflictException;
import com.ezekiel.fraudrisk.exception.ResourceNotFoundException;
import com.ezekiel.fraudrisk.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public CustomerResponse create(CustomerRequest request) {
        repository.findByEmailIgnoreCase(request.email()).ifPresent(existing -> {
            throw new ConflictException("A customer with this email already exists");
        });

        Customer customer = new Customer();
        apply(customer, request);
        if (customer.getAccountCreatedAt() == null) customer.setAccountCreatedAt(LocalDateTime.now());
        return toResponse(repository.save(customer));
    }

    public List<CustomerResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public CustomerResponse get(Long id) {
        return toResponse(require(id));
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = require(id);
        repository.findByEmailIgnoreCase(request.email()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) throw new ConflictException("A customer with this email already exists");
        });

        LocalDateTime existingAccountCreatedAt = customer.getAccountCreatedAt();
        apply(customer, request);
        if (request.accountCreatedAt() == null) customer.setAccountCreatedAt(existingAccountCreatedAt);

        return toResponse(repository.save(customer));
    }

    public void delete(Long id) {
        repository.delete(require(id));
    }

    public Customer require(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer " + id + " was not found"));
    }

    private void apply(Customer customer, CustomerRequest request) {
        customer.setEmail(request.email().trim());
        customer.setBillingAddress(request.billingAddress().trim());
        customer.setShippingAddress(request.shippingAddress().trim());
        customer.setAccountCreatedAt(request.accountCreatedAt());
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(), customer.getEmail(), customer.getBillingAddress(),
                customer.getShippingAddress(), customer.getAccountCreatedAt(), customer.getCreatedAt()
        );
    }
}
