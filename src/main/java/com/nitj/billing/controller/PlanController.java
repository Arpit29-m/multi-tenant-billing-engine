package com.nitj.billing.controller;

import com.nitj.billing.model.Plan;
import com.nitj.billing.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/tenants/{tenantId}/plans")
public class PlanController {
    @Autowired
    private PlanService planService;
    @PostMapping
    public ResponseEntity<Plan> createNewPlan(@PathVariable Long tenantId, @RequestBody Plan plan) {
        Plan savedPlan = planService.createPlan(tenantId, plan);
        return new ResponseEntity<>(savedPlan, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<Plan>> getAllPlansForTenant(@PathVariable Long tenantId) {
        List<Plan> plans = planService.getPlansByTenant(tenantId);
        return new ResponseEntity<>(plans, HttpStatus.OK);
    }
}
