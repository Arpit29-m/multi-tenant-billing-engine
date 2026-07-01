package com.nitj.billing.service;

import com.nitj.billing.enums.InvoiceStatus;
import com.nitj.billing.model.Customer;
import com.nitj.billing.model.Invoice;
import com.nitj.billing.model.Tenant;
import com.nitj.billing.repository.CustomerRepository;
import com.nitj.billing.repository.InvoiceRepository;
import com.nitj.billing.repository.TenantRepository;
import com.nitj.billing.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should successfully generate a multi-tenant invoice with correct mathematical calculations")
    public void testGenerateInvoice_Success() {
        Long tenantId = 1L;
        Long customerId = 5L;
        String description = "Gold Premium Plan Subscription Fee";
        Double baseAmount = 999.00;

        Tenant mockTenant = new Tenant();
        mockTenant.setId(tenantId);
        mockTenant.setCompanyName("Alpha Corp Workspace");

        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        mockCustomer.setName("Jane Doe");
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(mockTenant));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice generatedInvoice = invoiceService.generateInvoice(tenantId, customerId, description, baseAmount);

        assertNotNull(generatedInvoice, "The generated invoice should not be null.");
        assertEquals(InvoiceStatus.PENDING, generatedInvoice.getStatus(), "Initial invoice status must always be PENDING.");
        assertEquals(999.00, generatedInvoice.getTotalAmount(), 0.001, "The total calculated sum must match the base row element precisely.");
        assertEquals("Alpha Corp Workspace", generatedInvoice.getTenant().getCompanyName());
        assertEquals("Jane Doe", generatedInvoice.getCustomer().getName());

        assertTrue(generatedInvoice.getDueDate().isAfter(LocalDateTime.now().plusDays(13)), "The calendar due date must be offset securely by a 14-day grace window.");

        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should throw runtime exception immediately if an invalid tenant ID is passed")
    public void testGenerateInvoice_InvalidTenant_ThrowsException() {

        Long invalidTenantId = 999L;
        when(tenantRepository.findById(invalidTenantId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            invoiceService.generateInvoice(invalidTenantId, 1L, "Test Service", 50.0);
        });

        assertTrue(exception.getMessage().contains("Tenant with ID 999 not found."));
        verify(invoiceRepository, never()).save(any(Invoice.class)); // Ensure no broken row data was flushed to disk
    }
}