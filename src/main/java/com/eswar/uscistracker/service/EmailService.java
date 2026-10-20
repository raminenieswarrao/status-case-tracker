package com.eswar.uscistracker.service;

import com.eswar.uscistracker.domain.TrackedCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;
    private final ZoneId zoneId;

    private final DateTimeFormatter emailDateFormatter =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a z");

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.enabled:false}") boolean enabled,
            @Value("${app.mail.from:${spring.mail.username:}}") String from,
            @Value("${app.time-zone:America/New_York}") String timeZone) {

        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
        this.zoneId = ZoneId.of(timeZone);
    }

    public void sendStatusChange(TrackedCase c) {

        String friendlyStatus =
                c.getFriendlyStatus() == null
                        ? "UNKNOWN"
                        : c.getFriendlyStatus()
                        .name()
                        .replace('_', ' ');

        String subject =
                "USCIS Status Changed — "
                        + c.getCaseName()
                        + " — "
                        + friendlyStatus;

        String body = """
                Hello %s,

                USCIS has changed the status of your tracked case.

                Case: %s
                Applicant: %s
                Receipt: %s
                Status: %s
                USCIS Status: %s
                Status Changed: %s

                Details:
                %s

                Open the USCIS Case Tracker for the complete case history.

                This is an automated notification.
                """.formatted(
                safe(c.getApplicantName()),
                safe(c.getCaseName()),
                safe(c.getApplicantName()),
                mask(c.getReceiptNumber()),
                friendlyStatus,
                safe(c.getOfficialStatus()),
                formatDate(c.getLastStatusChangedAt()),
                safe(c.getOfficialDescription())
        );

        send(
                c.getNotificationEmail(),
                subject,
                body
        );
    }

    /*
     * Kept for compatibility with any older code that still calls
     * sendApproval(). New status-change logic should use
     * sendStatusChange().
     */
    public void sendApproval(TrackedCase c) {

        sendStatusChange(c);
    }

    public void sendDailySummary(
            String email,
            List<TrackedCase> cases) {

        if (cases == null || cases.isEmpty()) {
            return;
        }

        StringBuilder body =
                new StringBuilder(
                        "USCIS Case Status Summary\n\n"
                );

        for (TrackedCase c : cases) {

            body.append(safe(c.getCaseName()))
                    .append("\n")

                    .append("Applicant: ")
                    .append(safe(c.getApplicantName()))
                    .append("\n")

                    .append("Receipt: ")
                    .append(mask(c.getReceiptNumber()))
                    .append("\n")

                    .append("Status: ")
                    .append(
                            c.getFriendlyStatus() == null
                                    ? "UNKNOWN"
                                    : c.getFriendlyStatus()
                                    .name()
                                    .replace('_', ' ')
                    )
                    .append("\n")

                    .append("USCIS: ")
                    .append(safe(c.getOfficialStatus()))
                    .append("\n")

                    .append("Tracking: ")
                    .append(c.getTrackingStatus())
                    .append("\n")

                    .append("Last Checked: ")
                    .append(
                            formatDate(
                                    c.getLastCheckedAt()
                            )
                    )
                    .append("\n\n");
        }

        body.append(
                "Open the USCIS Case Tracker for complete case details."
        );

        send(
                email,
                "USCIS Case Status Summary",
                body.toString()
        );
    }

    private void send(
            String to,
            String subject,
            String body) {

        if (to == null || to.isBlank()) {

            System.err.printf(
                    "[MAIL SKIPPED] Empty recipient. Subject=%s%n",
                    subject
            );

            return;
        }

        /*
         * In local/mock development we print the email instead
         * of actually sending it.
         */
        if (!enabled) {

            System.out.printf(
                    "%n[MAIL DISABLED]%n"
                            + "To=%s%n"
                            + "Subject=%s%n"
                            + "%s%n",
                    to,
                    subject,
                    body
            );

            return;
        }

        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            if (from != null
                    && !from.isBlank()) {

                message.setFrom(from);
            }

            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            System.out.printf(
                    "[MAIL SENT] To=%s Subject=%s%n",
                    to,
                    subject
            );

        } catch (MailException ex) {

            /*
             * Important:
             * email failure should NOT cause a successful USCIS
             * status update to roll back.
             */
            System.err.printf(
                    "[MAIL ERROR] To=%s Subject=%s Error=%s%n",
                    to,
                    subject,
                    ex.getMessage()
            );
        }
    }

    private String formatDate(
            OffsetDateTime value) {

        if (value == null) {
            return "—";
        }

        return value
                .atZoneSameInstant(zoneId)
                .format(emailDateFormatter);
    }

    private String mask(
            String receipt) {

        if (receipt == null
                || receipt.length() < 7) {

            return "********";
        }

        return receipt.substring(0, 3)
                + "******"
                + receipt.substring(
                receipt.length() - 4
        );
    }

    private String safe(
            String value) {

        return value == null
                || value.isBlank()
                ? "—"
                : value;
    }
}