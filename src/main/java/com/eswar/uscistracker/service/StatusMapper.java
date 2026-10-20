package com.eswar.uscistracker.service;

import com.eswar.uscistracker.domain.FriendlyStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class StatusMapper {
    public FriendlyStatus map(String status, String description) {
        String text = ((status == null ? "" : status) + " " + (description == null ? "" : description))
            .toLowerCase(Locale.ROOT);

        if (containsAny(text, "invalid receipt", "receipt number entered is invalid", "not a valid receipt")) {
            return FriendlyStatus.INVALID_RECEIPT;
        }
        if (containsAny(text, "approved", "approval")) {
            return FriendlyStatus.APPROVED;
        }
        if (containsAny(text, "denied", "denial", "rejected")) {
            return FriendlyStatus.DENIED;
        }
        if (containsAny(text, "request for evidence", "rfe", "notice explaining", "action required",
                "request for initial evidence", "intent to deny")) {
            return FriendlyStatus.ACTION_REQUIRED;
        }
        if (containsAny(text, "response to uscis", "response was received", "evidence was received")) {
            return FriendlyStatus.RESPONSE_RECEIVED;
        }
        if (containsAny(text, "card was produced", "card was mailed", "case was closed",
                "completed", "certificate was issued")) {
            return FriendlyStatus.COMPLETED;
        }
        if (containsAny(text, "received", "acceptance")) {
            return FriendlyStatus.RECEIVED;
        }
        if (containsAny(text, "processing", "actively reviewed", "pending", "being reviewed",
                "transferred", "fingerprints", "biometrics")) {
            return FriendlyStatus.PROCESSING;
        }
        return FriendlyStatus.UNKNOWN;
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) if (text.contains(term)) return true;
        return false;
    }
}
