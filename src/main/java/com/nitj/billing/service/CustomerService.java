package com.nitj.billing.service;

import com.nitj.billing.model.Customer;
import com.nitj.billing.model.Tenant;
import com.nitj.billing.repository.CustomerRepository;
import com.nitj.billing.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TenantRepository tenantRepository;
    public Customer createCustomer(Long tenantId,Customer customer){
        Tenant tenant=tenantRepository.findById(tenantId).orElseThrow(()->new RuntimeException("Cannot register customer. Tenant with ID " + tenantId + " not found."));
        if(customerRepository.existsByEmailAndTenantId(customer.getEmail(),tenantId)){
            throw new RuntimeException("A customer with email '" + customer.getEmail() + "' is already registered under this business!");

        }
        customer.setTenant(tenant);
        return customerRepository.save(customer);
    }
    public List<Customer> getCustomersByTenant(Long tenantId){
        return customerRepository.findByTenantId(tenantId);
    }
}
