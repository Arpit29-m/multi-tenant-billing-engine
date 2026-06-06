package com.nitj.billing.controller;

import com.nitj.billing.model.Tenant;
import com.nitj.billing.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    @Autowired
    private TenantService tenantService;
    @PostMapping("/onboard")
    public ResponseEntity<Tenant> onboardNewBusiness(@RequestBody Tenant tenant) {
        Tenant savedTenant = tenantService.onboardTenant(tenant);
        return new ResponseEntity<>(savedTenant, HttpStatus.CREATED);
    }
}
