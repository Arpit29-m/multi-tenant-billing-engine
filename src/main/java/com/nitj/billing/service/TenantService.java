package com.nitj.billing.service;

import com.nitj.billing.model.Tenant;
import com.nitj.billing.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TenantService {
    @Autowired
    private TenantRepository tenantRepository;
    public Tenant onboardTenant(Tenant tenant) {
        if (tenantRepository.existsByAdminEmail(tenant.getAdminEmail())) {
            throw new RuntimeException("A business with this email is already registered!");
        }
        return tenantRepository.save(tenant);
    }
}
