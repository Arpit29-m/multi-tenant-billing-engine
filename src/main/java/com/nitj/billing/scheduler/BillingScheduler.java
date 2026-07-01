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

    // Daily Midnight Production Routine Rule
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
                    // 1. Establish precise historical billing period window dates
                    LocalDateTime periodStart = sub.getStartDate(); // or last renewal date
                    LocalDateTime periodEnd = sub.getNextBillingDate();

                    // 2. Base Flat-Rate Subscription Line Item Component
                    String flatDescription = sub.getPlan().getPlanName() + " - Base Subscription Fee";
                    Double flatPrice = sub.getPlan().getPrice();

                    // Instantiate master header via core service
                    Invoice consolidatedInvoice = invoiceService.generateInvoice(
                            sub.getTenant().getId(),
                            sub.getCustomer().getId(),
                            flatDescription,
                            flatPrice
                    );

                    // 3. Metered Consumption Extension Component
                    // Let's look up how many API calls they accumulated this period
                    String metricName = "API_CALLS";
                    Double totalUnitsUsed = usageService.getUsageSum(sub.getId(), metricName, periodStart, periodEnd);

                    if (totalUnitsUsed > 0) {
                        // SDE Business Rule: Suppose we charge ₹0.10 per custom API invocation unit
                        Double meteredRatePerUnit = 0.10;
                        Double dynamicCalculatedCharge = totalUnitsUsed * meteredRatePerUnit;

                        // Formulate extra line item row
                        InvoiceLineItem meteredRow = new InvoiceLineItem();
                        meteredRow.setDescription("Metered Overage: " + totalUnitsUsed.intValue() + " " + metricName + " invocations");
                        meteredRow.setAmount(dynamicCalculatedCharge);

                        // Append child record to existing parent container via cascading helper
                        consolidatedInvoice.addLineItem(meteredRow);

                        // 4. Re-calculate dynamic aggregate sum total on the entity object
                        Double updatedTotalSum = consolidatedInvoice.getLineItems().stream()
                                .mapToDouble(InvoiceLineItem::getAmount)
                                .sum();
                        consolidatedInvoice.setTotalAmount(updatedTotalSum);
                    }

                    // 5. Save advanced state mutations and advance contract lifecycle timestamp
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