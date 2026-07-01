package com.nitj.billing.scheduler;

import com.nitj.billing.enums.SubscriptionStatus;
import com.nitj.billing.model.Invoice;
import com.nitj.billing.model.InvoiceLineItem;
import com.nitj.billing.model.Subscription;
import com.nitj.billing.repository.SubscriptionRepository;
import com.nitj.billing.service.InvoiceService;
import com.nitj.billing.service.UsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BillingScheduler {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private UsageService usageService;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processDailySubscriptionBilling() {
        System.out.println("[CRON WORKER] Running automated consumption lookups...");

        List<Subscription> allSubscriptions = subscriptionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Subscription sub : allSubscriptions) {
            if (sub.getStatus() == SubscriptionStatus.ACTIVE &&
                    sub.getNextBillingDate() != null &&
                    sub.getNextBillingDate().isBefore(now)) {

                try {
                    LocalDateTime periodStart = sub.getStartDate(); // or last renewal date
                    LocalDateTime periodEnd = sub.getNextBillingDate();


                    String flatDescription = sub.getPlan().getPlanName() + " - Base Subscription Fee";
                    Double flatPrice = sub.getPlan().getPrice();


                    Invoice consolidatedInvoice = invoiceService.generateInvoice(
                            sub.getTenant().getId(),
                            sub.getCustomer().getId(),
                            flatDescription,
                            flatPrice
                    );

                    String metricName = "API_CALLS";
                    Double totalUnitsUsed = usageService.getUsageSum(sub.getId(), metricName, periodStart, periodEnd);

                    if (totalUnitsUsed > 0) {
                        Double meteredRatePerUnit = 0.10;
                        Double dynamicCalculatedCharge = totalUnitsUsed * meteredRatePerUnit;

                        InvoiceLineItem meteredRow = new InvoiceLineItem();
                        meteredRow.setDescription("Metered Overage: " + totalUnitsUsed.intValue() + " " + metricName + " invocations");
                        meteredRow.setAmount(dynamicCalculatedCharge);

                        consolidatedInvoice.addLineItem(meteredRow);

                        Double updatedTotalSum = consolidatedInvoice.getLineItems().stream()
                                .mapToDouble(InvoiceLineItem::getAmount)
                                .sum();
                        consolidatedInvoice.setTotalAmount(updatedTotalSum);
                    }
                    if ("ANNUAL".equalsIgnoreCase(sub.getPlan().getBillingCycle())) {
                        sub.setNextBillingDate(sub.getNextBillingDate().plusYears(1));
                    } else {
                        sub.setNextBillingDate(sub.getNextBillingDate().plusMonths(1));
                    }

                    subscriptionRepository.save(sub);
                    System.out.println("[CRON WORKER] Fully consolidated automated statement generated for Sub ID: " + sub.getId());

                } catch (Exception e) {
                    System.err.println("[CRON WORKER FAILURE] Error processing subscription ID: " + sub.getId() + " - " + e.getMessage());
                }
            }
        }
    }
}