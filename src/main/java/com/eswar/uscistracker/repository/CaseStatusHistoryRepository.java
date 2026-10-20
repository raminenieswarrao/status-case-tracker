package com.eswar.uscistracker.repository;

import com.eswar.uscistracker.domain.CaseStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseStatusHistoryRepository extends JpaRepository<CaseStatusHistory, Long> {
    List<CaseStatusHistory> findAllByTrackedCaseIdOrderByDetectedAtDesc(Long trackedCaseId);
}
