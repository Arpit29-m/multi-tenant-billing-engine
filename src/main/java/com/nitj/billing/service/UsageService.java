package com.nitj.billing.service;

import com.nitj.billing.enums.SubscriptionStatus;
import com.nitj.billing.model.Subscription;
import com.nitj.billing.model.UsageEvent;
import com.nitj.billing.repository.SubscriptionRepository;
import com.nitj.billing.repository.UsageEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UsageService {
    @Autowired
    private UsageEventRepository usageEventRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Transactional
    public UsageEvent recordUsage(Long tenantId, Long customerId, String metricName, Double quantity){
        Subscription activeSubscription = subscriptionRepository.findAll().stream()
                .filter(sub -> sub.getTenant().getId().equals(tenantId) &&
                        sub.getCustomer().getId().equals(customerId) &&
                        sub.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active subscription contract found for this customer. Cannot ingest usage."));
        // 2. Map and drop the usage record into our engine
        UsageEvent event = new UsageEvent();
        event.setSubscription(activeSubscription);
        event.setMetricName(metricName);
        event.setQuantity(quantity);

        return usageEventRepository.save(event);
    }
    public Double getUsageSum(Long subscriptionId, String metricName, LocalDateTime start, LocalDateTime end){
        return usageEventRepository.getAccumulatedUsage(subscriptionId, metricName, start, end);
    }
}
