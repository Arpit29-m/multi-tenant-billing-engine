package com.nitj.billing.repository;

import com.nitj.billing.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {
    List<Subscription> findByTenantId(Long tenantId);
    Optional<Subscription> findByCustomerIdAndStatus(Long customerId, com.nitj.billing.enums.SubscriptionStatus status);
}
