package com.nitj.billing.controller;
import com.nitj.billing.model.Invoice;
import com.nitj.billing.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/tenants/{tenantId}/invoices")
public class InvoiceController {
    @Autowired
    private InvoiceService invoiceService;
    @PostMapping
    public ResponseEntity<Invoice> createManualInvoice(
            @PathVariable Long tenantId,
            @RequestParam Long customerId,
            @RequestParam String description,
            @RequestParam Double amount) {

        Invoice customInvoice = invoiceService.generateInvoice(tenantId, customerId, description, amount);
        return new ResponseEntity<>(customInvoice, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices(@PathVariable Long tenantId) {
        List<Invoice> invoices = invoiceService.getInvoicesByTenant(tenantId);
        return new ResponseEntity<>(invoices, HttpStatus.OK);
    }
}
