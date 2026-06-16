package com.nitj.billing.controller;

import com.nitj.billing.model.Subscription;
import com.nitj.billing.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/tenants/{tenantId}/subscriptions")
public class SubscriptionController {
    @Autowired
    private SubscriptionService subscriptionService;
    @PostMapping
    public ResponseEntity<Subscription> startSubscription(
            @PathVariable Long tenantId,
            @RequestParam Long customerId,
            @RequestParam Long planId) {

        Subscription activeSub = subscriptionService.createSubscription(tenantId, customerId, planId);
        return new ResponseEntity<>(activeSub, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<Subscription>> getAllSubscriptions(@PathVariable Long tenantId) {
        List<Subscription> subs = subscriptionService.getSubscriptionsByTenant(tenantId);
        return new ResponseEntity<>(subs, HttpStatus.OK);
    }
}
