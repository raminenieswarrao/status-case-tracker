package com.eswar.uscistracker.service;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service("dateTimeFormatterService")
public class DateTimeFormatterService {

    private static final ZoneId EASTERN =
            ZoneId.of("America/New_York");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    public String format(OffsetDateTime dateTime) {

        if (dateTime == null) {
            return "—";
        }

        return dateTime
                .atZoneSameInstant(EASTERN)
                .format(FORMATTER);
    }
}