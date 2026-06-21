package com.nitj.billing.repository;

import com.nitj.billing.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
    List<Invoice> findByTenantId(Long tenantId);
    List<Invoice> findByCustomerId(Long customerId);
}
