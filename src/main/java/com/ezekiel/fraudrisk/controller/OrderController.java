package com.ezekiel.fraudrisk.controller;

import com.ezekiel.fraudrisk.dto.OrderRequest;
import com.ezekiel.fraudrisk.dto.OrderResponse;
import com.ezekiel.fraudrisk.dto.OrderUpdateRequest;
import com.ezekiel.fraudrisk.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderRequest request) { return service.create(request); }

    @GetMapping
    public List<OrderResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) { return service.get(id); }

    @GetMapping("/customer/{customerId}")
    public List<OrderResponse> listByCustomer(@PathVariable Long customerId) { return service.listByCustomer(customerId); }

    @PutMapping("/{id}")
    public OrderResponse update(@PathVariable Long id, @Valid @RequestBody OrderUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
