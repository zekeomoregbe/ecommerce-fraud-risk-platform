package com.ezekiel.fraudrisk.controller;

import com.ezekiel.fraudrisk.dto.CustomerRequest;
import com.ezekiel.fraudrisk.dto.CustomerResponse;
import com.ezekiel.fraudrisk.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) { return service.create(request); }

    @GetMapping
    public List<CustomerResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) { return service.get(id); }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
