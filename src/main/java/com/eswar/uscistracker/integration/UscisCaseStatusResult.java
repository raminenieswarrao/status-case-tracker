package com.eswar.uscistracker.integration;

import java.time.OffsetDateTime;

public record UscisCaseStatusResult(
    boolean valid,
    String receiptNumber,
    String formType,
    String officialStatus,
    String officialDescription,
    OffsetDateTime uscisUpdatedAt,
    String errorMessage
) {
    public static UscisCaseStatusResult invalid(String receipt, String message) {
        return new UscisCaseStatusResult(false, receipt, null,
            "Invalid Case Number", message, null, message);
    }
}
