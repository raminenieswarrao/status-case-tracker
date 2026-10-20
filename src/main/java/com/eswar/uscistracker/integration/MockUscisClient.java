package com.eswar.uscistracker.integration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Locale;

@Component
@ConditionalOnProperty(name = "uscis.mode", havingValue = "mock", matchIfMissing = true)
public class MockUscisClient implements UscisClient {

    @Override
    public UscisCaseStatusResult check(String receiptNumber) {
        String r = receiptNumber.toUpperCase(Locale.ROOT);

        // Useful for UI testing without sending any real receipt number anywhere.
        if (r.endsWith("0000")) {
            return UscisCaseStatusResult.invalid(r,
                "The receipt number entered is invalid. Please verify the number and try again.");
        }
        if (r.endsWith("0001")) {
            return new UscisCaseStatusResult(true, r, "I-129",
                "Case Was Approved",
                "We approved your Form I-129, Petition for a Nonimmigrant Worker.",
                OffsetDateTime.now().minusDays(1), null);
        }
        if (r.endsWith("0002")) {
            return new UscisCaseStatusResult(true, r, "I-129",
                "Request for Evidence Was Sent",
                "We sent a request for additional evidence for your case.",
                OffsetDateTime.now().minusHours(3), null);
        }
        return new UscisCaseStatusResult(true, r, "I-129",
            "USCIS Is Currently Processing the Case",
            "USCIS is currently processing your Form I-129. We do not currently need anything from you.",
            OffsetDateTime.now().minusDays(2), null);
    }
}
