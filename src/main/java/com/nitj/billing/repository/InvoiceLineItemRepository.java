package com.nitj.billing.repository;

import com.nitj.billing.model.InvoiceLineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem,Long> {
    List<InvoiceLineItem> findByInvoiceId(Long invoiceId);
}
