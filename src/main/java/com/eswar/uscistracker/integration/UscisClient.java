package com.eswar.uscistracker.integration;

public interface UscisClient {
    UscisCaseStatusResult check(String receiptNumber);
}
