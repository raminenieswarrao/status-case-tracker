package com.eswar.uscistracker.service;

import com.eswar.uscistracker.domain.TrackedCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CaseScheduler {

    private final CaseService caseService;
    private final EmailService emailService;

    public CaseScheduler(
            CaseService caseService,
            EmailService emailService) {

        this.caseService = caseService;
        this.emailService = emailService;
    }

    /*
     * Runs every hour.
     *
     * CaseService only loads TrackingStatus.ACTIVE,
     * so PAUSED, INVALID_RECEIPT and COMPLETED cases
     * are automatically skipped.
     */
    @Scheduled(
            cron = "${app.schedule.hourly-cron:0 0 * * * *}",
            zone = "${app.time-zone:America/New_York}"
    )
    public void hourlyStatusRefresh() {

        caseService.refreshActiveCases();
    }

    /*
     * Runs every day at 5:00 PM Eastern by default.
     */
    @Scheduled(
            cron = "${app.schedule.daily-email-cron:0 0 17 * * *}",
            zone = "${app.time-zone:America/New_York}"
    )
    public void dailyEmailSummary() {

        for (Map.Entry<String, List<TrackedCase>> entry
                : caseService
                .groupForDailyEmail()
                .entrySet()) {

            emailService.sendDailySummary(
                    entry.getKey(),
                    entry.getValue()
            );
        }
    }
}