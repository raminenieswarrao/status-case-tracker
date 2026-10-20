package com.eswar.uscistracker.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CaseForm {
    @NotBlank
    @Size(max = 120)
    private String caseName;

    @NotBlank
    @Size(max = 120)
    private String applicantName;

    @NotBlank
    @Email
    @Size(max = 254)
    private String notificationEmail;

    @NotBlank
    @Pattern(regexp = "(?i)^[A-Z]{3}[0-9]{10}$",
             message = "Use 3 letters followed by 10 numbers, for example IOE1234567890.")
    private String receiptNumber;

    public String getCaseName() { return caseName; }
    public void setCaseName(String caseName) { this.caseName = caseName; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getNotificationEmail() { return notificationEmail; }
    public void setNotificationEmail(String notificationEmail) { this.notificationEmail = notificationEmail; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
}
