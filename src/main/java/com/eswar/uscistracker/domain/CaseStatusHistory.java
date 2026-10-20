package com.eswar.uscistracker.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "case_status_history",
       indexes = @Index(name = "idx_history_case_detected", columnList = "tracked_case_id,detected_at"))
public class CaseStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tracked_case_id", nullable = false)
    private TrackedCase trackedCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "friendly_status", nullable = false, length = 40)
    private FriendlyStatus friendlyStatus;

    @Column(name = "official_status", length = 500)
    private String officialStatus;

    @Column(name = "official_description", columnDefinition = "text")
    private String officialDescription;

    @Column(name = "uscis_updated_at")
    private OffsetDateTime uscisUpdatedAt;

    @Column(name = "detected_at", nullable = false)
    private OffsetDateTime detectedAt = OffsetDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TrackedCase getTrackedCase() { return trackedCase; }
    public void setTrackedCase(TrackedCase trackedCase) { this.trackedCase = trackedCase; }
    public FriendlyStatus getFriendlyStatus() { return friendlyStatus; }
    public void setFriendlyStatus(FriendlyStatus friendlyStatus) { this.friendlyStatus = friendlyStatus; }
    public String getOfficialStatus() { return officialStatus; }
    public void setOfficialStatus(String officialStatus) { this.officialStatus = officialStatus; }
    public String getOfficialDescription() { return officialDescription; }
    public void setOfficialDescription(String officialDescription) { this.officialDescription = officialDescription; }
    public OffsetDateTime getUscisUpdatedAt() { return uscisUpdatedAt; }
    public void setUscisUpdatedAt(OffsetDateTime uscisUpdatedAt) { this.uscisUpdatedAt = uscisUpdatedAt; }
    public OffsetDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(OffsetDateTime detectedAt) { this.detectedAt = detectedAt; }
}
