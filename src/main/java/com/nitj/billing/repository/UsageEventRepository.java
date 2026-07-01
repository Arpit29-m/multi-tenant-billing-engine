package com.nitj.billing.repository;

import com.nitj.billing.model.UsageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface UsageEventRepository extends JpaRepository<UsageEvent,Long> {
    @Query("SELECT COALESCE(SUM(u.quantity), 0.0) FROM UsageEvent u " +
            "WHERE u.subscription.id = :subscriptionId " +
            "AND u.metricName = :metricName " +
            "AND u.timestamp BETWEEN :startDate AND :endDate")
    Double getAccumulatedUsage(
            @Param("subscriptionId") Long subscriptionId,
            @Param("metricName") String metricName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
