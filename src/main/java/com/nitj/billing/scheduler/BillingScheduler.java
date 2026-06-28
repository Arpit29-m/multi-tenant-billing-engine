package com.nitj.billing.scheduler;

import com.nitj.billing.enums.SubscriptionStatus;
import com.nitj.billing.model.Subscription;
import com.nitj.billing.repository.SubscriptionRepository;
import com.nitj.billing.service.InvoiceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class BillingScheduler {

    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private InvoiceService invoiceService;
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processDailySubscriptionBilling(){
        System.out.println("Wake-up call is triggered:" + LocalDateTime.now());
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for(Subscription sub: allSubscriptions){
            if(sub.getStatus()== SubscriptionStatus.ACTIVE&&sub.getNextBillingDate()!=null&&sub.getNextBillingDate().isBefore(now)) {
                System.out.println("Found due contract! Processing customer ID" + sub.getCustomer().getId() + "for tenants" + sub.getTenant().getId());
                try {
                    String billingNarration = sub.getPlan().getPlanName() + " - Recurring Renewal Charge";
                    Double recurringCost = sub.getPlan().getPrice();

                    invoiceService.generateInvoice(
                            sub.getTenant().getId(),
                            sub.getCustomer().getId(),
                            billingNarration,
                            recurringCost
                    );
                    if ("ANNUAL".equalsIgnoreCase(sub.getPlan().getBillingCycle())) {
                        sub.setNextBillingDate(sub.getNextBillingDate().plusYears(1));
                    } else {
                        sub.setNextBillingDate(sub.getNextBillingDate().plusMonths(1));
                    }

                    subscriptionRepository.save(sub);
                    System.out.println("[CRON WORKER] Invoice generated successfully. Subscription advanced.");
                } catch (Exception e) {
                    System.err.println("[CRON WORKER ERROR] Failed to process subscription ID: "
                            + sub.getId() + ". Error: " + e.getMessage());

                }
            }
        }
        System.out.println("Subscription evaluation cycle complete");
    }
}
