package com.nitj.billing.repository;

import com.nitj.billing.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {
    List<Customer> findByTenantId(Long tenantId);
    boolean existsByEmailAndTenantId(String email, Long tenantId);
}
