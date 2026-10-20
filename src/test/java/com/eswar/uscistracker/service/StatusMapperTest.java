package com.eswar.uscistracker.service;

import com.eswar.uscistracker.domain.FriendlyStatus;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class StatusMapperTest {
    private final StatusMapper mapper = new StatusMapper();

    @Test void mapsApproval() {
        assertThat(mapper.map("Case Was Approved", "")).isEqualTo(FriendlyStatus.APPROVED);
    }

    @Test void mapsProcessing() {
        assertThat(mapper.map("USCIS Is Currently Processing the Case", "")).isEqualTo(FriendlyStatus.PROCESSING);
    }

    @Test void mapsRfe() {
        assertThat(mapper.map("Request for Evidence Was Sent", "")).isEqualTo(FriendlyStatus.ACTION_REQUIRED);
    }

    @Test void mapsInvalid() {
        assertThat(mapper.map("Invalid receipt number", "")).isEqualTo(FriendlyStatus.INVALID_RECEIPT);
    }
}
