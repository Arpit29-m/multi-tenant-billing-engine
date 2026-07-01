package com.nitj.billing.controller;

import com.nitj.billing.model.UsageEvent;
import com.nitj.billing.service.UsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/tenants/{tenantId}/usage")
public class UsageController {
    @Autowired
    private UsageService usageService;
    @PostMapping("/events")
    public ResponseEntity<UsageEvent> ingestUsageEvent(
            @PathVariable Long tenantId,
            @RequestParam Long customerId,
            @RequestParam String metricName,
            @RequestParam Double quantity) {

        UsageEvent recordedEvent = usageService.recordUsage(tenantId, customerId, metricName, quantity);
        return new ResponseEntity<>(recordedEvent, HttpStatus.CREATED);
    }
}
