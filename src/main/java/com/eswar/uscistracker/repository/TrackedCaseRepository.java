package com.eswar.uscistracker.repository;

import com.eswar.uscistracker.domain.TrackedCase;
import com.eswar.uscistracker.domain.TrackingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackedCaseRepository extends JpaRepository<TrackedCase, Long> {
    List<TrackedCase> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<TrackedCase> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndReceiptNumberIgnoreCase(Long userId, String receiptNumber);
    List<TrackedCase> findAllByTrackingStatus(TrackingStatus trackingStatus);
    List<TrackedCase> findAllByNotificationEmailIgnoreCaseOrderByCreatedAtAsc(String notificationEmail);
    List<TrackedCase> findAllByOrderByNotificationEmailAscCreatedAtAsc();
}
