package com.nitj.billing.service;

import com.nitj.billing.enums.InvoiceStatus;
import com.nitj.billing.model.Customer;
import com.nitj.billing.model.Invoice;
import com.nitj.billing.model.InvoiceLineItem;
import com.nitj.billing.model.Tenant;
import com.nitj.billing.repository.CustomerRepository;
import com.nitj.billing.repository.InvoiceRepository;
import com.nitj.billing.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceService {
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public Invoice generateInvoice(Long tenantId,Long customerId,String itemDescription,Double baseAmount){
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant with ID " + tenantId + " not found."));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer with ID " + customerId + " not found."));

        Invoice invoice = new Invoice();
        invoice.setTenant(tenant);
        invoice.setCustomer(customer);
        invoice.setStatus(InvoiceStatus.PENDING);

        invoice.setDueDate(LocalDateTime.now().plusDays(14));

        InvoiceLineItem subscriptionCharge = new InvoiceLineItem();
        subscriptionCharge.setDescription(itemDescription);
        subscriptionCharge.setAmount(baseAmount);

        invoice.addLineItem(subscriptionCharge);
        Double totalSum = invoice.getLineItems().stream()
                .mapToDouble(InvoiceLineItem::getAmount)
                .sum();
        invoice.setTotalAmount(totalSum);

        // 6. Push to PostgreSQL database
        return invoiceRepository.save(invoice);
    }
    public List<Invoice> getInvoicesByTenant(Long tenantId) {
        return invoiceRepository.findByTenantId(tenantId);
    }
}
