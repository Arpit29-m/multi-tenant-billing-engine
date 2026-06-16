package com.nitj.billing.controller;

import com.nitj.billing.model.Customer;
import com.nitj.billing.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/tenants/{tenantId}/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    @PostMapping
    public ResponseEntity<Customer> registerCustomer(@PathVariable Long tenantId, @RequestBody Customer customer) {
        Customer savedCustomer = customerService.createCustomer(tenantId, customer);
        return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
    }
    @GetMapping
        public ResponseEntity<List<Customer>> getCustomersForTenant(@PathVariable Long tenantId) {
            List<Customer> customers = customerService.getCustomersByTenant(tenantId);
            return new ResponseEntity<>(customers, HttpStatus.OK);
    }
}
