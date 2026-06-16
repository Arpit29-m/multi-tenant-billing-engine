package com.nitj.billing.service;

import com.nitj.billing.enums.SubscriptionStatus;
import com.nitj.billing.model.Customer;
import com.nitj.billing.model.Plan;
import com.nitj.billing.model.Subscription;
import com.nitj.billing.model.Tenant;
import com.nitj.billing.repository.CustomerRepository;
import com.nitj.billing.repository.PlanRepository;
import com.nitj.billing.repository.SubscriptionRepository;
import com.nitj.billing.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionService {
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private PlanRepository planRepository;
    public Subscription createSubscription(Long tenantId,Long customerId,Long planId){
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant with ID " + tenantId + " not found."));
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer with ID " + customerId + " not found."));
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan with ID " + planId + " not found."));
        subscriptionRepository.findByCustomerIdAndStatus(customerId, SubscriptionStatus.ACTIVE)
                .ifPresent(s -> {
                    throw new RuntimeException("Customer already has an ACTIVE subscription (ID: " + s.getId() + "). Cancel or upgrade it first.");
                });
        Subscription subscription = new Subscription();
        subscription.setTenant(tenant);
        subscription.setCustomer(customer);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();
        subscription.setStartDate(now);
        if ("ANNUAL".equalsIgnoreCase(plan.getBillingCycle())) {
            subscription.setNextBillingDate(now.plusYears(1));
        } else{
            subscription.setNextBillingDate(now.plusMonths(1));
        }
        return subscriptionRepository.save(subscription);
    }
    public List<Subscription> getSubscriptionsByTenant(Long tenantId) {
        return subscriptionRepository.findByTenantId(tenantId);
    }
}
