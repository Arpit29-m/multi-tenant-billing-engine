package com.nitj.billing.service;

import com.nitj.billing.model.Plan;
import com.nitj.billing.model.Tenant;
import com.nitj.billing.repository.PlanRepository;
import com.nitj.billing.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanService {
    @Autowired
    private PlanRepository planRepository;
    @Autowired
    private TenantRepository tenantRepository;
    public Plan createPlan(Long tenantId,Plan plan){
        Tenant tenant=tenantRepository.findById(tenantId).orElseThrow(() -> new RuntimeException("Cannot create plan. Tenant with ID " + tenantId + " not found."));
        plan.setTenant(tenant);
        return planRepository.save(plan);
    }
    public List<Plan> getPlansByTenant(Long tenantId) {
        return planRepository.findByTenantId(tenantId);
    }
}
