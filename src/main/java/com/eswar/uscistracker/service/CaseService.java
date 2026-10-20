package com.eswar.uscistracker.service;

import com.eswar.uscistracker.domain.AppUser;
import com.eswar.uscistracker.domain.CaseStatusHistory;
import com.eswar.uscistracker.domain.FriendlyStatus;
import com.eswar.uscistracker.domain.TrackedCase;
import com.eswar.uscistracker.domain.TrackingStatus;
import com.eswar.uscistracker.integration.UscisCaseStatusResult;
import com.eswar.uscistracker.integration.UscisClient;
import com.eswar.uscistracker.repository.CaseStatusHistoryRepository;
import com.eswar.uscistracker.repository.TrackedCaseRepository;
import com.eswar.uscistracker.web.CaseForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class CaseService {

    private final TrackedCaseRepository cases;
    private final CaseStatusHistoryRepository history;
    private final UscisClient uscisClient;
    private final StatusMapper statusMapper;
    private final EmailService emailService;

    public CaseService(
            TrackedCaseRepository cases,
            CaseStatusHistoryRepository history,
            UscisClient uscisClient,
            StatusMapper statusMapper,
            EmailService emailService) {

        this.cases = cases;
        this.history = history;
        this.uscisClient = uscisClient;
        this.statusMapper = statusMapper;
        this.emailService = emailService;
    }

    public List<TrackedCase> listForUser(
            Long userId) {

        return cases
                .findAllByUserIdOrderByCreatedAtDesc(
                        userId
                );
    }

    public TrackedCase requireOwned(
            Long id,
            Long userId) {

        return cases
                .findByIdAndUserId(
                        id,
                        userId
                )
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "Case not found."
                        )
                );
    }

    public List<CaseStatusHistory> historyFor(
            TrackedCase trackedCase) {

        return history
                .findAllByTrackedCaseIdOrderByDetectedAtDesc(
                        trackedCase.getId()
                );
    }

    /*
     * ==========================================================
     * CREATE
     * ==========================================================
     */

    @Transactional
    public TrackedCase create(
            AppUser user,
            CaseForm form) {

        String receipt =
                normalizeReceipt(
                        form.getReceiptNumber()
                );

        if (cases
                .existsByUserIdAndReceiptNumberIgnoreCase(
                        user.getId(),
                        receipt
                )) {

            throw new IllegalArgumentException(
                    "You are already tracking this receipt number."
            );
        }

        TrackedCase trackedCase =
                new TrackedCase();

        trackedCase.setUser(user);

        applyForm(
                trackedCase,
                form,
                receipt
        );

        trackedCase =
                cases.save(trackedCase);

        /*
         * Initial check.
         *
         * The initial NEW -> PROCESSING/RECEIVED/etc transition
         * is NOT treated as an email status-change alert.
         */
        return refreshInternal(
                trackedCase,
                true
        );
    }

    /*
     * ==========================================================
     * UPDATE CASE
     * ==========================================================
     */

    @Transactional
    public TrackedCase update(
            Long id,
            AppUser user,
            CaseForm form) {

        TrackedCase trackedCase =
                requireOwned(
                        id,
                        user.getId()
                );

        String newReceipt =
                normalizeReceipt(
                        form.getReceiptNumber()
                );

        boolean receiptChanged =
                !newReceipt.equalsIgnoreCase(
                        trackedCase.getReceiptNumber()
                );

        if (receiptChanged
                && cases
                .existsByUserIdAndReceiptNumberIgnoreCase(
                        user.getId(),
                        newReceipt
                )) {

            throw new IllegalArgumentException(
                    "You are already tracking this receipt number."
            );
        }

        applyForm(
                trackedCase,
                form,
                newReceipt
        );

        /*
         * If receipt changed, treat it like a newly validated case.
         */
        if (receiptChanged) {

            trackedCase.setTrackingStatus(
                    TrackingStatus.ACTIVE
            );

            trackedCase.setFriendlyStatus(
                    FriendlyStatus.NEW
            );

            trackedCase.setApprovalNotificationSent(
                    false
            );

            trackedCase.setLastError(
                    null
            );

            trackedCase =
                    cases.save(trackedCase);

            return refreshInternal(
                    trackedCase,
                    true
            );
        }

        return cases.save(
                trackedCase
        );
    }

    /*
     * ==========================================================
     * MANUAL REFRESH
     * ==========================================================
     */

    @Transactional
    public TrackedCase refreshOwned(
            Long id,
            AppUser user) {

        return refreshInternal(
                requireOwned(
                        id,
                        user.getId()
                ),
                true
        );
    }

    /*
     * ==========================================================
     * DELETE
     * ==========================================================
     */

    @Transactional
    public void delete(
            Long id,
            AppUser user) {

        TrackedCase trackedCase =
                requireOwned(
                        id,
                        user.getId()
                );

        cases.delete(
                trackedCase
        );
    }

    /*
     * ==========================================================
     * TRACKING PAUSE / RESUME
     * ==========================================================
     */

    @Transactional
    public void pause(
            Long id,
            AppUser user) {

        TrackedCase trackedCase =
                requireOwned(
                        id,
                        user.getId()
                );

        if (trackedCase.getTrackingStatus()
                == TrackingStatus.COMPLETED) {

            throw new IllegalArgumentException(
                    "This case has already reached a final status."
            );
        }

        if (trackedCase.getTrackingStatus()
                == TrackingStatus.INVALID_RECEIPT) {

            throw new IllegalArgumentException(
                    "Fix the invalid receipt number before changing tracking."
            );
        }

        trackedCase.setTrackingStatus(
                TrackingStatus.PAUSED
        );

        cases.save(
                trackedCase
        );
    }

    @Transactional
    public void resume(
            Long id,
            AppUser user) {

        TrackedCase trackedCase =
                requireOwned(
                        id,
                        user.getId()
                );

        if (trackedCase.getFriendlyStatus()
                == FriendlyStatus.INVALID_RECEIPT) {

            throw new IllegalArgumentException(
                    "Fix the invalid receipt number before resuming tracking."
            );
        }

        if (trackedCase.getTrackingStatus()
                == TrackingStatus.COMPLETED) {

            throw new IllegalArgumentException(
                    "This case has reached a final status and automatic tracking is complete."
            );
        }

        trackedCase.setTrackingStatus(
                TrackingStatus.ACTIVE
        );

        cases.save(
                trackedCase
        );
    }

    /*
     * ==========================================================
     * EMAIL SETTINGS
     * ==========================================================
     */

    @Transactional
    public TrackedCase updateNotificationSettings(
            Long id,
            AppUser user,
            boolean dailySummaryEnabled,
            boolean statusChangeEmailEnabled) {

        TrackedCase trackedCase =
                requireOwned(
                        id,
                        user.getId()
                );

        trackedCase.setDailySummaryEnabled(
                dailySummaryEnabled
        );

        trackedCase.setStatusChangeEmailEnabled(
                statusChangeEmailEnabled
        );

        return cases.save(
                trackedCase
        );
    }

    @Transactional
    public TrackedCase pauseEmailNotifications(
            Long id,
            AppUser user) {

        TrackedCase trackedCase =
                requireOwned(
                        id,
                        user.getId()
                );

        trackedCase.setEmailNotificationsPaused(
                true
        );

        return cases.save(
                trackedCase
        );
    }

    @Transactional
    public TrackedCase resumeEmailNotifications(
            Long id,
            AppUser user) {

        TrackedCase trackedCase =
                requireOwned(
                        id,
                        user.getId()
                );

        trackedCase.setEmailNotificationsPaused(
                false
        );

        return cases.save(
                trackedCase
        );
    }

    /*
     * ==========================================================
     * HOURLY SCHEDULER
     * ==========================================================
     */

    @Transactional
    public void refreshActiveCases() {

        List<TrackedCase> activeCases =
                cases.findAllByTrackingStatus(
                        TrackingStatus.ACTIVE
                );

        for (TrackedCase trackedCase
                : activeCases) {

            try {

                refreshInternal(
                        trackedCase,
                        false
                );

                /*
                 * Conservative request pacing.
                 */
                Thread.sleep(
                        220
                );

            } catch (InterruptedException ex) {

                Thread.currentThread()
                        .interrupt();

                return;

            } catch (Exception ex) {

                trackedCase.setLastError(
                        shortMessage(
                                ex.getMessage()
                        )
                );

                trackedCase.setLastCheckedAt(
                        OffsetDateTime.now()
                );

                cases.save(
                        trackedCase
                );
            }
        }
    }

    /*
     * ==========================================================
     * CORE USCIS REFRESH
     * ==========================================================
     */

    @Transactional
    public TrackedCase refreshInternal(
            TrackedCase trackedCase,
            boolean propagateErrors) {

        try {

            FriendlyStatus oldStatus =
                    trackedCase.getFriendlyStatus();

            TrackingStatus previousTrackingStatus =
                    trackedCase.getTrackingStatus();

            UscisCaseStatusResult result =
                    uscisClient.check(
                            trackedCase.getReceiptNumber()
                    );

            trackedCase.setLastCheckedAt(
                    OffsetDateTime.now()
            );

            trackedCase.setLastError(
                    null
            );

            /*
             * ==================================================
             * INVALID RECEIPT
             * ==================================================
             */

            if (!result.valid()) {

                boolean statusChanged =
                        oldStatus
                                != FriendlyStatus.INVALID_RECEIPT;

                boolean initialStatus =
                        oldStatus
                                == FriendlyStatus.NEW;

                trackedCase.setFriendlyStatus(
                        FriendlyStatus.INVALID_RECEIPT
                );

                trackedCase.setTrackingStatus(
                        TrackingStatus.INVALID_RECEIPT
                );

                trackedCase.setOfficialStatus(
                        "Invalid Case Number"
                );

                trackedCase.setOfficialDescription(
                        result.errorMessage()
                );

                if (statusChanged
                        || initialStatus) {

                    trackedCase.setLastStatusChangedAt(
                            OffsetDateTime.now()
                    );

                    trackedCase =
                            cases.save(
                                    trackedCase
                            );

                    saveHistory(
                            trackedCase
                    );

                } else {

                    trackedCase =
                            cases.save(
                                    trackedCase
                            );
                }

                /*
                 * Do not email when a user initially enters an invalid
                 * case. They already see the result immediately.
                 *
                 * But if a previously valid case somehow becomes invalid,
                 * treat it as a real change.
                 */
                if (statusChanged
                        && !initialStatus) {

                    sendStatusChangeEmailIfEnabled(
                            trackedCase
                    );
                }

                return trackedCase;
            }

            /*
             * ==================================================
             * VALID RECEIPT
             * ==================================================
             */

            FriendlyStatus mappedStatus =
                    statusMapper.map(
                            result.officialStatus(),
                            result.officialDescription()
                    );

            trackedCase.setFormType(
                    result.formType()
            );

            trackedCase.setOfficialStatus(
                    result.officialStatus()
            );

            trackedCase.setOfficialDescription(
                    result.officialDescription()
            );

            trackedCase.setUscisUpdatedAt(
                    result.uscisUpdatedAt()
            );

            trackedCase.setFriendlyStatus(
                    mappedStatus
            );

            /*
             * Final statuses stop future hourly checks.
             */
            if (isFinalStatus(
                    mappedStatus)) {

                trackedCase.setTrackingStatus(
                        TrackingStatus.COMPLETED
                );

            } else if (previousTrackingStatus
                    == TrackingStatus.PAUSED) {

                /*
                 * Manual Refresh Status should not accidentally
                 * resume a case the user intentionally paused.
                 */
                trackedCase.setTrackingStatus(
                        TrackingStatus.PAUSED
                );

            } else {

                trackedCase.setTrackingStatus(
                        TrackingStatus.ACTIVE
                );
            }

            boolean statusChanged =
                    oldStatus
                            != mappedStatus;

            boolean initialStatus =
                    oldStatus
                            == FriendlyStatus.NEW;

            /*
             * Save history only when friendly status changes.
             */
            if (statusChanged
                    || initialStatus) {

                trackedCase.setLastStatusChangedAt(
                        OffsetDateTime.now()
                );

                trackedCase =
                        cases.save(
                                trackedCase
                        );

                saveHistory(
                        trackedCase
                );

            } else {

                trackedCase =
                        cases.save(
                                trackedCase
                        );
            }

            /*
             * Real status change after initial case creation.
             */
            if (statusChanged
                    && !initialStatus) {

                sendStatusChangeEmailIfEnabled(
                        trackedCase
                );
            }

            /*
             * Legacy DB field retained because it already exists.
             *
             * We no longer use this field to decide whether
             * status-change emails are sent.
             */
            if (mappedStatus
                    == FriendlyStatus.APPROVED) {

                trackedCase
                        .setApprovalNotificationSent(
                                true
                        );

                trackedCase =
                        cases.save(
                                trackedCase
                        );
            }

            return trackedCase;

        } catch (RuntimeException ex) {

            trackedCase.setLastCheckedAt(
                    OffsetDateTime.now()
            );

            trackedCase.setLastError(
                    shortMessage(
                            ex.getMessage()
                    )
            );

            cases.save(
                    trackedCase
            );

            if (propagateErrors) {
                throw ex;
            }

            return trackedCase;
        }
    }

    /*
     * ==========================================================
     * STATUS CHANGE EMAIL
     * ==========================================================
     */

    private void sendStatusChangeEmailIfEnabled(
            TrackedCase trackedCase) {

        /*
         * Whole email system is paused for this case.
         */
        if (trackedCase
                .isEmailNotificationsPaused()) {

            return;
        }

        /*
         * User disabled Status Change Alerts.
         */
        if (!trackedCase
                .isStatusChangeEmailEnabled()) {

            return;
        }

        emailService.sendStatusChange(
                trackedCase
        );
    }

    /*
     * ==========================================================
     * FINAL STATUS
     * ==========================================================
     */

    private boolean isFinalStatus(
            FriendlyStatus status) {

        return status
                == FriendlyStatus.APPROVED

                || status
                == FriendlyStatus.DENIED

                || status
                == FriendlyStatus.COMPLETED;
    }

    /*
     * ==========================================================
     * DAILY 5 PM EMAIL
     * ==========================================================
     */

    public Map<String, List<TrackedCase>>
    groupForDailyEmail() {

        Map<String, List<TrackedCase>> grouped =
                new LinkedHashMap<>();

        for (TrackedCase trackedCase
                : cases
                .findAllByOrderByNotificationEmailAscCreatedAtAsc()) {

            /*
             * Daily Summary OFF.
             */
            if (!trackedCase
                    .isDailySummaryEnabled()) {

                continue;
            }

            /*
             * Email paused.
             */
            if (trackedCase
                    .isEmailNotificationsPaused()) {

                continue;
            }

            /*
             * Final case:
             *
             * Status-change alert already handled the final event.
             * Do not keep emailing the same approved/denied case daily.
             */
            if (trackedCase.getTrackingStatus()
                    == TrackingStatus.COMPLETED) {

                continue;
            }

            /*
             * Invalid receipt:
             *
             * We do not want to repeatedly send an invalid receipt
             * summary every evening.
             */
            if (trackedCase.getTrackingStatus()
                    == TrackingStatus.INVALID_RECEIPT) {

                continue;
            }

            String notificationEmail =
                    trackedCase
                            .getNotificationEmail();

            if (notificationEmail == null
                    || notificationEmail.isBlank()) {

                continue;
            }

            grouped
                    .computeIfAbsent(
                            notificationEmail
                                    .toLowerCase(),
                            key ->
                                    new ArrayList<>()
                    )
                    .add(
                            trackedCase
                    );
        }

        return grouped;
    }

    /*
     * ==========================================================
     * HELPERS
     * ==========================================================
     */

    private void applyForm(
            TrackedCase trackedCase,
            CaseForm form,
            String receipt) {

        trackedCase.setCaseName(
                form.getCaseName()
                        .trim()
        );

        trackedCase.setApplicantName(
                form.getApplicantName()
                        .trim()
        );

        trackedCase.setNotificationEmail(
                form.getNotificationEmail()
                        .trim()
                        .toLowerCase()
        );

        trackedCase.setReceiptNumber(
                receipt
        );
    }

    private void saveHistory(
            TrackedCase trackedCase) {

        CaseStatusHistory statusHistory =
                new CaseStatusHistory();

        statusHistory.setTrackedCase(
                trackedCase
        );

        statusHistory.setFriendlyStatus(
                trackedCase.getFriendlyStatus()
        );

        statusHistory.setOfficialStatus(
                trackedCase.getOfficialStatus()
        );

        statusHistory.setOfficialDescription(
                trackedCase.getOfficialDescription()
        );

        statusHistory.setUscisUpdatedAt(
                trackedCase.getUscisUpdatedAt()
        );

        history.save(
                statusHistory
        );
    }

    private String normalizeReceipt(
            String receipt) {

        if (receipt == null) {
            return "";
        }

        return receipt
                .replace(
                        "-",
                        ""
                )
                .replace(
                        " ",
                        ""
                )
                .trim()
                .toUpperCase();
    }

    private String shortMessage(
            String message) {

        if (message == null) {
            return "Unknown error";
        }

        return message.length() > 950
                ? message.substring(
                0,
                950
        )
                : message;
    }
}