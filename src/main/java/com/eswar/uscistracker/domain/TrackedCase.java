package com.eswar.uscistracker.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "tracked_cases",
        indexes = {
                @Index(name = "idx_tracked_cases_user", columnList = "user_id"),
                @Index(name = "idx_tracked_cases_tracking", columnList = "tracking_status"),
                @Index(name = "idx_tracked_cases_receipt", columnList = "receipt_number")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_receipt",
                        columnNames = {"user_id", "receipt_number"}
                )
        }
)
public class TrackedCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "case_name", nullable = false, length = 120)
    private String caseName;

    @Column(name = "applicant_name", nullable = false, length = 120)
    private String applicantName;

    @Column(name = "notification_email", nullable = false, length = 254)
    private String notificationEmail;

    @Column(name = "receipt_number", nullable = false, length = 30)
    private String receiptNumber;

    @Column(name = "form_type", length = 40)
    private String formType;

    @Enumerated(EnumType.STRING)
    @Column(name = "friendly_status", nullable = false, length = 40)
    private FriendlyStatus friendlyStatus = FriendlyStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "tracking_status", nullable = false, length = 40)
    private TrackingStatus trackingStatus = TrackingStatus.ACTIVE;

    @Column(name = "official_status", length = 500)
    private String officialStatus;

    @Column(name = "official_description", columnDefinition = "text")
    private String officialDescription;

    @Column(name = "uscis_updated_at")
    private OffsetDateTime uscisUpdatedAt;

    @Column(name = "last_checked_at")
    private OffsetDateTime lastCheckedAt;

    @Column(name = "last_status_changed_at")
    private OffsetDateTime lastStatusChangedAt;

    @Column(name = "approval_notification_sent", nullable = false)
    private boolean approvalNotificationSent = false;

    /*
     * EMAIL NOTIFICATION SETTINGS
     *
     * dailySummaryEnabled:
     * Send one summary email every day at the configured application time.
     *
     * statusChangeEmailEnabled:
     * Send an email immediately when the USCIS case status changes.
     *
     * emailNotificationsPaused:
     * Temporarily stop all email notifications for this case without
     * disabling USCIS case tracking.
     */

    @Column(name = "daily_summary_enabled", nullable = false)
    private boolean dailySummaryEnabled = true;

    @Column(name = "status_change_email_enabled", nullable = false)
    private boolean statusChangeEmailEnabled = true;

    @Column(name = "email_notifications_paused", nullable = false)
    private boolean emailNotificationsPaused = false;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public FriendlyStatus getFriendlyStatus() {
        return friendlyStatus;
    }

    public void setFriendlyStatus(FriendlyStatus friendlyStatus) {
        this.friendlyStatus = friendlyStatus;
    }

    public TrackingStatus getTrackingStatus() {
        return trackingStatus;
    }

    public void setTrackingStatus(TrackingStatus trackingStatus) {
        this.trackingStatus = trackingStatus;
    }

    public String getOfficialStatus() {
        return officialStatus;
    }

    public void setOfficialStatus(String officialStatus) {
        this.officialStatus = officialStatus;
    }

    public String getOfficialDescription() {
        return officialDescription;
    }

    public void setOfficialDescription(String officialDescription) {
        this.officialDescription = officialDescription;
    }

    public OffsetDateTime getUscisUpdatedAt() {
        return uscisUpdatedAt;
    }

    public void setUscisUpdatedAt(OffsetDateTime uscisUpdatedAt) {
        this.uscisUpdatedAt = uscisUpdatedAt;
    }

    public OffsetDateTime getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(OffsetDateTime lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public OffsetDateTime getLastStatusChangedAt() {
        return lastStatusChangedAt;
    }

    public void setLastStatusChangedAt(OffsetDateTime lastStatusChangedAt) {
        this.lastStatusChangedAt = lastStatusChangedAt;
    }

    public boolean isApprovalNotificationSent() {
        return approvalNotificationSent;
    }

    public void setApprovalNotificationSent(boolean approvalNotificationSent) {
        this.approvalNotificationSent = approvalNotificationSent;
    }

    public boolean isDailySummaryEnabled() {
        return dailySummaryEnabled;
    }

    public void setDailySummaryEnabled(boolean dailySummaryEnabled) {
        this.dailySummaryEnabled = dailySummaryEnabled;
    }

    public boolean isStatusChangeEmailEnabled() {
        return statusChangeEmailEnabled;
    }

    public void setStatusChangeEmailEnabled(boolean statusChangeEmailEnabled) {
        this.statusChangeEmailEnabled = statusChangeEmailEnabled;
    }

    public boolean isEmailNotificationsPaused() {
        return emailNotificationsPaused;
    }

    public void setEmailNotificationsPaused(boolean emailNotificationsPaused) {
        this.emailNotificationsPaused = emailNotificationsPaused;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}